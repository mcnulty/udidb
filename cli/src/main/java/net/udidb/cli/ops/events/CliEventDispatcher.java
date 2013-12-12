/*
 * Copyright (c) 2011-2013, Dan McNulty
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the UDI project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package net.udidb.cli.ops.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessManager;
import net.libudi.api.UdiThread;
import net.libudi.api.event.EventType;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.event.UdiEventVisitor;
import net.libudi.api.exceptions.UdiException;
import net.udidb.cli.context.GlobalContextManager;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.events.EventObserver;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.results.Result;

/**
 * Class that dispatches events that occur in a process
 *
 * @author mcnulty
 */
@Singleton
public class CliEventDispatcher implements EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(CliEventDispatcher.class);

    private final UdiProcessManager processManager;

    private final GlobalContextManager contextManager;

    private final UdiEventVisitor defaultEventVisitor;

    private boolean blockForEvent = true;

    private boolean displayAllEvents = false;

    private EventThread eventThread = null;

    private final Map<UdiProcess, Set<EventObserver>> eventObservers = new HashMap<>();

    @Inject
    CliEventDispatcher(UdiProcessManager processManager, GlobalContextManager contextManager, UdiEventVisitor defaultEventVisitor) {
        this.processManager = processManager;
        this.contextManager = contextManager;
        this.defaultEventVisitor = defaultEventVisitor;
    }

    public boolean isBlockForEvent() {
        return blockForEvent;
    }

    public void setBlockForEvent(boolean blockForEvent) {
        this.blockForEvent = blockForEvent;
    }

    public boolean isDisplayAllEvents() {
        return displayAllEvents;
    }

    public void setDisplayAllEvents(boolean displayAllEvents) {
        this.displayAllEvents = displayAllEvents;
    }

    private EventThread getEventThread() {
        if (eventThread == null) {
            synchronized (this) {
                if (eventThread == null) {
                    eventThread = new EventThread(processManager, contextManager);
                    eventThread.start();
                }
            }
        }
        return eventThread;
    }

    private void registerEventObserver(EventObserver eventObserver) {
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
    public void handleEvents(Result result) throws UdiException, OperationException {

        if (result.getDeferredEventObserver() != null) {
            registerEventObserver(result.getDeferredEventObserver());
        }

        EventThread localThread = getEventThread();


        if (blockForEvent) {
            // Only attempt to block when an event is pending
            if (result.isEventPending()) {
                // Attempt to retrieve the first event via blocking
                UdiEvent event = null;
                while (event == null) {
                    try {
                        event = localThread.getEvents().take();
                        dispatchEvent(event);
                    }catch (InterruptedException e) {
                    }
                }
            }
        }

        UdiEvent event;
        while ((event = localThread.getEvents().poll()) != null) {
            dispatchEvent(event);
        }
    }

    private void dispatchEvent(UdiEvent event) throws UdiException, OperationException {
        if (event instanceof WaitError) {
            throw ((WaitError)event).getException();
        }

        Set<EventObserver> observers = eventObservers.get(event.getProcess());

        boolean observersNotified = false;
        synchronized (observers) {
            if (observers != null) {
                Iterator<EventObserver> i = observers.iterator();
                while (i.hasNext()) {
                    EventObserver eventObserver = i.next();
                    if (!eventObserver.publish(event)) {
                        i.remove();
                    }
                    observersNotified = true;
                }
            }
        }

        if (displayAllEvents || !observersNotified) {
            event.accept(defaultEventVisitor);
        }
    }

    /**
     * Fake UdiEvent used to report errors from the event thread
     */
    private static class WaitError implements UdiEvent {

        private final UdiException exception;

        public WaitError(UdiException exception) {
            this.exception = exception;
        }

        private UdiException getException() {
            return exception;
        }

        @Override
        public EventType getEventType() {
            return null;
        }

        @Override
        public UdiProcess getProcess() {
            return null;
        }

        @Override
        public UdiThread getThread() {
            return null;
        }

        @Override
        public void accept(UdiEventVisitor visitor) {
        }
    }

    /**
     * Thread used when running in non-blocking event handling mode
     */
    private static class EventThread extends Thread {

        private final LinkedBlockingQueue<UdiEvent> events = new LinkedBlockingQueue<>();

        private final GlobalContextManager contextManager;

        private final UdiProcessManager processManager;

        private volatile boolean terminate = false;

        public EventThread(UdiProcessManager processManager, GlobalContextManager contextManager) {
            this.contextManager = contextManager;
            this.processManager = processManager;
            setDaemon(true);
        }

        public void terminate() {
            terminate = true;

            while (this.isAlive()) {
                try {
                   this.join();
                }catch (InterruptedException e) {
                    log.debug("Interrupted.", e);
                }
            }
        }

        public LinkedBlockingQueue<UdiEvent> getEvents() {
            return events;
        }

        @Override
        public void run() {
            while (!terminate) {
                try {
                    List<UdiEvent> newEvents = processManager.waitForEvents(contextManager.getProcesses());

                    for (UdiEvent event : newEvents) {
                        events.add(event);
                    }
                }catch (UdiException e) {
                    events.add(new WaitError(e));
                }
            }
        }
    }
}
