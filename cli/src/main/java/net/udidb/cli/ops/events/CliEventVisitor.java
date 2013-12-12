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

import java.io.PrintStream;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.event.UdiEventError;
import net.libudi.api.event.UdiEventProcessExit;
import net.libudi.api.event.UdiEventThreadCreate;
import net.libudi.api.event.UdiEventVisitor;

/**
 * The event visitor for the CLI
 *
 * @author mcnulty
 */
public class CliEventVisitor implements UdiEventVisitor {

    private final PrintStream out;

    @Inject
    CliEventVisitor(@Named("OUTPUT DESTINATION") PrintStream out) {
        this.out = out;
    }

    @Override
    public void visit(UdiEventBreakpoint breakpointEvent) {
        out.println(String.format("Breakpoint at 0x%x", breakpointEvent.getAddress()));
    }

    @Override
    public void visit(UdiEventError errorEvent) {
        out.println(String.format("Error event occurred: %s", errorEvent.getErrorString()));
    }

    @Override
    public void visit(UdiEventProcessExit processExitEvent) {
        out.println(String.format("Process exited with code = %d", processExitEvent.getExitCode()));
    }

    @Override
    public void visit(UdiEventThreadCreate threadCreateEvent) {
        out.println(String.format("Thread created id = 0x%x", threadCreateEvent.getNewThread().getTid()));
    }
}
