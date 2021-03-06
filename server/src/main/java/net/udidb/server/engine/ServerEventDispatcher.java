/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.libudi.api.UdiProcess;
import net.libudi.api.event.UdiEvent;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.events.EventObserver;
import net.udidb.engine.events.EventSink;
import net.udidb.engine.ops.OperationException;
import net.udidb.server.api.models.UdiEventModel;
import ws.wamp.jawampa.WampClient;

/**
 * Background thread that publishes events
 */
@Singleton
public class ServerEventDispatcher extends Thread implements EventSink
{
    private static final Logger logger = LoggerFactory.getLogger(ServerEventDispatcher.class);

    private static final String GLOBAL_TOPIC = "com.udidb.events";

    private final WampClient wampClient;
    private final ServerState serverState;
    private final BlockingQueue<Notification> notifications = new LinkedBlockingQueue<>();
    private final Map<UdiProcess, EventContext> eventContexts = new HashMap<>();

    @Inject
    public ServerEventDispatcher(WampClient wampClient,
                                 ServerState serverState)
    {
        this.wampClient = wampClient;
        this.serverState = serverState;

        this.setName(ServerEventDispatcher.class.getSimpleName());
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void accept(List<UdiEvent> events)
    {
        Notification notification = new Notification();
        notification.events = new LinkedList<>(events);
        notifications.add(notification);
    }

    public void registerEventObserver(EventObserver eventObserver)
    {
        Notification notification = new Notification();
        notification.eventObserver = eventObserver;
        notifications.add(notification);
    }

    public void readyForEvent(DebuggeeContext debuggeeContext)
    {
        if (debuggeeContext != null) {
            Notification notification = new Notification();
            notification.debuggeeContext = debuggeeContext;
            notifications.add(notification);
        }
    }

    @Override
    public void run()
    {
        while(true) {

            Notification notification;
            try {
                notification = notifications.take();
            }catch (InterruptedException e) {
                logger.debug("Interrupted while waiting for notification", e);
                continue;
            }

            try {
                if (notification.events != null) {
                    handleEvents(notification.events);
                } else if (notification.eventObserver != null) {
                    handleEventObserver(notification.eventObserver);
                } else if (notification.debuggeeContext != null) {
                    handleReadyForEvents(notification.debuggeeContext);
                } else {
                    logger.warn("Invalid notification received");
                    throw new IllegalStateException("Invalid notification received");
                }
            } catch (OperationException e) {
                throw new IllegalStateException("Failed to handle event", e);
            }
        }
    }

    private void handleEvents(List<UdiEvent> events) throws OperationException
    {
        for (UdiEvent event : events) {
            EventContext eventContext = getEventContext(event.getProcess());
            eventContext.events.add(event);
        }

        for (EventContext eventContext : eventContexts.values()) {
            publishIfReady(eventContext);
        }
    }

    private void handleEventObserver(EventObserver eventObserver) throws OperationException
    {
        EventContext eventContext = getEventContext(eventObserver.getDebuggeeContext().getProcess());
        eventContext.eventObservers.add(eventObserver);

        publishIfReady(eventContext);
    }

    private void handleReadyForEvents(DebuggeeContext debuggeeContext) throws OperationException
    {
        EventContext eventContext = getEventContext(debuggeeContext.getProcess());
        eventContext.readyForEvents = true;

        publishIfReady(eventContext);
    }

    private void publishIfReady(EventContext context) throws OperationException
    {
        logger.debug("{}", context);
        if (context.readyForEvents && context.events.size() > 0) {
            Iterator<UdiEvent> eventIterator = context.events.iterator();
            while (eventIterator.hasNext()) {
                UdiEvent event = eventIterator.next();

                Iterator<EventObserver> observerIterator = context.eventObservers.iterator();
                while (observerIterator.hasNext()) {
                    EventObserver observer = observerIterator.next();
                    try {
                        if (!observer.publish(event)) {
                            observerIterator.remove();
                        }
                    }catch (OperationException e) {
                        logger.error("{} failed to handle published event", observer, e);
                        observerIterator.remove();
                    }
                }

                serverState.completePendingOperation(event);
                publishEvent(event);
                eventIterator.remove();

                handleTermination(event);
            }

            context.readyForEvents = false;
        }
    }

    private void publishEvent(UdiEvent udiEvent) throws OperationException
    {
        wampClient.publish(GLOBAL_TOPIC, new UdiEventModel(udiEvent)).subscribe(
                publicationId -> logger.debug("{} successfully published with id {}", udiEvent, publicationId),
                error -> logger.error("Failed to publish event", error)
        );
    }

    private void handleTermination(UdiEvent udiEvent) throws OperationException
    {
        boolean termination = false;
        switch (udiEvent.getEventType()) {
            case PROCESS_CLEANUP:
                termination = true;
                break;
            default:
                break;
        }

        if (termination) {
            eventContexts.remove(udiEvent.getProcess());

            int pid;
            try {
                pid = udiEvent.getProcess().getPid();
                udiEvent.getProcess().close();
            } catch (Exception e) {
                throw new OperationException("Failed to cleanup terminated process", e);
            }
            logger.debug("Debuggee[pid={}] terminated after event {}",
                         pid,
                         udiEvent.getEventType());
        }
    }

    private EventContext getEventContext(UdiProcess process)
    {
        EventContext eventContext = eventContexts.get(process);
        if (eventContext == null) {
            eventContext = new EventContext();
            eventContexts.put(process, eventContext);
        }
        return eventContext;
    }

    private static class Notification
    {
        private List<UdiEvent> events;

        private EventObserver eventObserver;

        private DebuggeeContext debuggeeContext;
    }

    private static class EventContext
    {
        private final List<UdiEvent> events = new LinkedList<>();

        private final List<EventObserver> eventObservers = new LinkedList<>();

        private boolean readyForEvents = false;

        @Override
        public String toString()
        {
            return "EventContext{[" +
                    events.stream().map(UdiEvent::getEventType).map(Object::toString).collect(Collectors.joining(",")) +
                    "]," +
                    eventObservers +
                    "," +
                    "readyForEvents=" + readyForEvents +
                    "}";
        }
    }
}
