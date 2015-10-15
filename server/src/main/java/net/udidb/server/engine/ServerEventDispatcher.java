/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessManager;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.events.DbEventData;
import net.udidb.engine.events.EventObserver;
import net.udidb.engine.ops.OperationException;
import net.udidb.server.api.models.UdiEventModel;
import ws.wamp.jawampa.WampClient;

/**
 * @author mcnulty
 */
@Singleton
public class ServerEventDispatcher extends Thread
{
    private static final String GLOBAL_TOPIC = "com.udidb.events";

    private static final Logger logger = LoggerFactory.getLogger(ServerEventDispatcher.class);

    private final Map<UdiProcess, Set<EventObserver>> eventObservers = new HashMap<>();

    private final UdiProcessManager processManager;
    private final DebuggeeContextManager debuggeeContextManager;
    private final WampClient wampClient;

    @Inject
    public ServerEventDispatcher(DebuggeeContextManager debuggeeContextManager,
                                 UdiProcessManager processManager,
                                 WampClient wampClient)
    {
        this.processManager = processManager;
        this.debuggeeContextManager = debuggeeContextManager;
        this.wampClient = wampClient;
        this.setDaemon(true);
        this.start();
    }

    public void registerEventObserver(EventObserver eventObserver)
    {
        Set<EventObserver> procEventObs;
        synchronized (eventObservers) {
            procEventObs = eventObservers.get(eventObserver.getDebuggeeContext().getProcess());
            if (procEventObs == null) {
                procEventObs = new HashSet<>();
                eventObservers.put(eventObserver.getDebuggeeContext().getProcess(), procEventObs);
            }
        }
        procEventObs.add(eventObserver);
    }

    @Override
    public void run()
    {
        while(true) {
            List<UdiProcess> processes = debuggeeContextManager.getEventContexts().stream()
                    .map(DebuggeeContext::getProcess)
                    .collect(Collectors.toList());

            synchronized (this) {
                while(processes.size() == 0) {
                    try {
                        logger.debug("Waiting for processes to exist");
                        wait();
                        processes = debuggeeContextManager.getEventContexts().stream()
                                .map(DebuggeeContext::getProcess)
                                .collect(Collectors.toList());
                        logger.debug("{} processes exist", processes.size());
                    }catch (InterruptedException e) {
                        logger.debug("Interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            List<UdiEvent> udiEvents;
            try {
                logger.debug("Waiting for events");
                udiEvents = processManager.waitForEvents(processes);
            }catch (UdiException e) {
                logger.warn("Failed to wait for events", e);
                continue;
            }

            for (UdiEvent udiEvent : udiEvents) {
                logger.debug("Processing event {} for process {}", udiEvent.getEventType(), udiEvent.getProcess().getPid());
                try {
                    DbEventData dbEventData = new DbEventData();
                    udiEvent.setUserData(dbEventData);

                    synchronized (eventObservers) {
                        Set<EventObserver> observers = eventObservers.get(udiEvent.getProcess());
                        if (observers != null) {
                            synchronized (observers) {
                                Iterator<EventObserver> i = observers.iterator();
                                while (i.hasNext()) {
                                    EventObserver eventObserver = i.next();
                                    if (!eventObserver.publish(udiEvent)) {
                                        i.remove();
                                    }
                                }
                            }
                        }
                    }

                    publishEvent(udiEvent);

                    handleTermination(udiEvent);
                }catch (OperationException e) {
                    logger.warn("Failed to handle event {}",
                            udiEvent.getEventType(),
                            e);
                }
            }
        }
    }

    private void publishEvent(UdiEvent udiEvent)
    {
        wampClient.publish(GLOBAL_TOPIC, new UdiEventModel(udiEvent)).subscribe(
                publicationId -> logger.debug("{} successfully published with id {}", udiEvent, publicationId),
                error -> logger.error("Failed to publish event", error)
        );
    }

    private void handleTermination(UdiEvent udiEvent) throws OperationException {
        boolean termination = false;
        switch (udiEvent.getEventType()) {
            case PROCESS_CLEANUP:
                termination = true;
                break;
            default:
                break;
        }

        if (termination) {
            DebuggeeContext debuggeeContext = debuggeeContextManager.deleteContext(udiEvent.getProcess());

            try {
                udiEvent.getProcess().close();
            } catch (Exception e) {
                throw new OperationException("Failed to cleanup terminated process", e);
            }
            logger.debug("Debuggee[{}] terminated after event {}",
                    debuggeeContext.getId(),
                    udiEvent.getEventType());
        }
    }

    public synchronized void notifyOfEvents()
    {
        notifyAll();
    }
}
