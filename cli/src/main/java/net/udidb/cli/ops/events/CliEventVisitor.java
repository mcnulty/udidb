/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.events;

import java.io.PrintStream;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import net.libudi.api.event.UdiEvent;
import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.event.UdiEventError;
import net.libudi.api.event.UdiEventProcessCleanup;
import net.libudi.api.event.UdiEventProcessExit;
import net.libudi.api.event.UdiEventThreadCreate;
import net.libudi.api.event.UdiEventVisitor;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.events.DbEventData;

/**
 * The event visitor for the CLI
 */
public class CliEventVisitor implements UdiEventVisitor {

    private final PrintStream out;

    private boolean displayAllEvents = false;

    @Inject
    CliEventVisitor(@Named("OUTPUT DESTINATION") PrintStream out) {
        this.out = out;
    }

    private boolean displayEvent(UdiEvent udiEvent) {
        if (udiEvent.getUserData() instanceof DbEventData) {
            DbEventData dbEventData = (DbEventData) udiEvent.getUserData();
            if (dbEventData.isIntermediateEvent()) {
                if (displayAllEvents) {
                    return true;
                }else{
                    return false;
                }
            }else{
                return true;
            }
        }

        return false;
    }

    @Override
    public void visit(UdiEventBreakpoint breakpointEvent) {
        if (displayEvent(breakpointEvent)) {
            out.println(String.format("Breakpoint hit at 0x%x", breakpointEvent.getAddress()));
        }
    }

    @Override
    public void visit(UdiEventError errorEvent) {
        if (displayEvent(errorEvent)) {
            out.println(String.format("Error event occurred: %s", errorEvent.getErrorString()));
        }
    }

    @Override
    public void visit(UdiEventProcessExit processExitEvent) {
        if (displayEvent(processExitEvent)) {
            out.println(String.format("Process exiting with code = %d", processExitEvent.getExitCode()));
        }
    }

    @Override
    public void visit(UdiEventThreadCreate threadCreateEvent) {
        if (displayEvent(threadCreateEvent)) {
            try {
                out.println(String.format("Thread created id = 0x%x", threadCreateEvent.getNewThread().getTid()));
            } catch (UdiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void visit(UdiEventProcessCleanup processCleanup) {
        if (displayEvent(processCleanup)) {
            try {
                out.println(String.format("Process %d terminated", processCleanup.getProcess().getPid()));
            } catch (UdiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isDisplayAllEvents() {
        return displayAllEvents;
    }

    public void setDisplayAllEvents(boolean displayAllEvents) {
        this.displayAllEvents = displayAllEvents;
    }
}
