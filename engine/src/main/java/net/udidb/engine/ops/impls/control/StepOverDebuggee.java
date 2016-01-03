/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.control;

import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiThread;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.event.UdiEventProcessCleanup;
import net.libudi.api.event.UdiEventProcessExit;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.machinecode.MachineCodeMapping;
import net.sourcecrumbs.api.machinecode.SourceLineRange;
import net.sourcecrumbs.api.transunit.NoSuchLineException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextAware;
import net.udidb.engine.events.DbEventData;
import net.udidb.engine.events.EventObserver;
import net.udidb.engine.ops.MissingDebugInfoException;
import net.udidb.engine.ops.NoDebuggeeContextException;
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
public class StepOverDebuggee extends DisplayNameOperation implements EventObserver, DebuggeeContextAware
{

    private static final Logger logger = LoggerFactory.getLogger(StepOverDebuggee.class);

    private DebuggeeContext context;
    private MachineCodeMapping machineCodeMapping;
    private final OperationResultVisitor resultVisitor;
    private final SourceLineRowFactory sourceLineRowFactory;

    @Inject
    public StepOverDebuggee(OperationResultVisitor resultVisitor,
            SourceLineRowFactory sourceLineRowFactory)
    {
        this.resultVisitor = resultVisitor;
        this.sourceLineRowFactory = sourceLineRowFactory;
    }

    @Override
    public Result execute() throws OperationException {

        if (context == null) {
            throw new NoDebuggeeContextException();
        }

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

        DbEventData dbEventData = (DbEventData) event.getUserData();

        if (context == null) {
            throw new NoDebuggeeContextException();
        }

        if (machineCodeMapping == null) {
            throw new MissingDebugInfoException();
        }

        try {
            // Don't implement a full-on event visitor since this operation just cares about breakpoints
            if ((event instanceof UdiEventProcessExit) || (event instanceof UdiEventProcessCleanup)) {
                // At this point, if these events are received, they indicate the operation could not be completed
                return false;
            }

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
            dbEventData.setIntermediateEvent(true);
            return false;
        }catch (UdiException e) {
            throw new OperationException("Failed to handle next statement completion breakpoint", e);
        }
    }

    @Override
    public void setDebuggeeContext(DebuggeeContext debuggeeContext)
    {
        this.context = debuggeeContext;
        this.machineCodeMapping = debuggeeContext.getExecutable().getMachineCodeMapping();
    }
}
