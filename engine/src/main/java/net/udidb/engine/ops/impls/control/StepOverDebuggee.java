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

package net.udidb.engine.ops.impls.control;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiThread;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.machinecode.MachineCodeMapping;
import net.sourcecrumbs.api.machinecode.SourceLineRange;
import net.sourcecrumbs.api.transunit.NoSuchLineException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.events.EventObserver;
import net.udidb.engine.ops.MissingDebugInfoException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.source.SourceLineRow;
import net.udidb.engine.source.SourceLineRowFactory;

/**
 * Operation to execute the next statement of source for a debuggee, stepping over method calls
 *
 * @author mcnulty
 */
@HelpMessage(enMessage="Step over the next statement in the debuggee")
@LongHelpMessage(enMessage=
        "next\n\n" +
        "Execute the next statement of source for the current debuggee, stepping over method calls"
)
@DisplayName("next")
public class StepOverDebuggee extends DisplayNameOperation implements EventObserver {

    private static final Logger logger = LoggerFactory.getLogger(StepOverDebuggee.class);

    private final DebuggeeContext context;
    private final OperationResultVisitor resultVisitor;
    private final MachineCodeMapping machineCodeMapping;
    private final SourceLineRowFactory sourceLineRowFactory;

    @Inject
    public StepOverDebuggee(DebuggeeContext context, OperationResultVisitor resultVisitor,
            SourceLineRowFactory sourceLineRowFactory)
    {
        this.context = context;
        this.resultVisitor = resultVisitor;
        this.machineCodeMapping = context.getExecutable().getMachineCodeMapping();
        this.sourceLineRowFactory = sourceLineRowFactory;
    }

    @Override
    public Result execute() throws OperationException {

        if (machineCodeMapping == null) {
            throw new MissingDebugInfoException();
        }

        try {
            // Get the current pc
            UdiThread thread = context.getCurrentThread();
            long pc = thread.getPC();

            // Determine pc of the destination statement to step to
            long nextPc = machineCodeMapping.getNextStatementAddress(pc);
            if (nextPc == 0) {
                throw new OperationException("Failed to locate line number information for next statement");
            }

            // Set breakpoint at destination
            UdiProcess process = context.getProcess();
            process.createBreakpoint(nextPc);
            process.installBreakpoint(nextPc);

            // Continue debuggee
            process.continueProcess();

            // Wait for the breakpoint to be hit before completing the operation
            return new DeferredResult(this);
        }catch(UdiException e) {
            throw new OperationException("Failed to set breakpoints for next statement execution", e);
        }
    }

    @Override
    public DebuggeeContext getDebuggeeContext() {
        return context;
    }

    @Override
    public boolean publish(UdiEvent event) throws OperationException {

        if (machineCodeMapping == null) {
            throw new MissingDebugInfoException();
        }

        try {
            // Don't implement a full-on event visitor since this operation just cares about breakpoints
            if (!(event instanceof UdiEventBreakpoint)) {
                throw new OperationException("Unexpected event encountered while waiting for breakpoint");
            }
            UdiEventBreakpoint brkptEvent = (UdiEventBreakpoint) event;
            long address = brkptEvent.getAddress();

            // Delete the breakpoint
            context.getProcess().deleteBreakpoint(address);

            // Propagates the result from the completion of the operation
            List<SourceLineRange> lineRanges = machineCodeMapping.getSourceLinesRanges(address);
            TableResult result;
            try {
                result = new TableResult(sourceLineRowFactory.create(lineRanges));
            }catch (NoSuchLineException e) {
                logger.debug("No line information available", e);
                result = new TableResult(new SourceLineRow());
            }
            resultVisitor.visit(this, result);

            // The Operation only cares about this event
            return false;
        }catch (UdiException e) {
            throw new OperationException("Failed to handle next statement completion breakpoint", e);
        }
    }
}
