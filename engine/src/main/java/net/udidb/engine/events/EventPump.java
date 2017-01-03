/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.events;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessManager;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.context.DebuggeeContextObserver;
import net.udidb.engine.ops.OperationException;

/**
 * A component that uses a dedicated thread to wait for events on all available processes.
 *
 * @author mcnulty
 */
public class EventPump extends Thread implements DebuggeeContextObserver
{
    private static final Logger logger = LoggerFactory.getLogger(EventPump.class);

    private final UdiProcessManager udiProcessManager;

    private final DebuggeeContextManager debuggeeContextManager;

    private final EventSink eventSink;

    @Inject
    public EventPump(UdiProcessManager udiProcessManager,
                     DebuggeeContextManager debuggeeContextManager,
                     EventSink eventSink)
    {
        this.udiProcessManager = udiProcessManager;
        this.eventSink = eventSink;
        this.debuggeeContextManager = debuggeeContextManager;
        this.debuggeeContextManager.addObserver(this);
        this.setName(EventPump.class.getSimpleName());
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run()
    {
        while (true) {
            List<UdiProcess> processes;
            do {
                processes = debuggeeContextManager.getContexts()
                                                  .values()
                                                  .stream()
                                                  .map(DebuggeeContext::getProcess)
                                                  .collect(Collectors.toList());
                if (processes.size() == 0) {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            logger.debug("Interrupted while waiting for event processes", e);
                            this.isInterrupted();
                        }
                    }
                }
            }while (processes.size() == 0);

            List<UdiEvent> events;
            try {
                logger.debug("Waiting for events");
                events = udiProcessManager.waitForEvents(processes);
            }catch (UdiException e) {
                logger.debug("Failed to wait for events", e);
                continue;
            }

            if (events.size() > 0) {
                events.stream().forEach(event -> event.setUserData(new DbEventData()));
                eventSink.accept(events);

                for (UdiEvent event : events) {
                    try {
                        handleTermination(event);
                    }catch (OperationException e) {
                        logger.warn("Failed to handle event {}",
                                event.getEventType(),
                                e);
                    }
                }
            }
        }
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

    @Override
    public void created(DebuggeeContext debuggeeContext)
    {
        this.interrupt();
    }

    @Override
    public void deleted(DebuggeeContext debuggeeContext)
    {

    }
}
