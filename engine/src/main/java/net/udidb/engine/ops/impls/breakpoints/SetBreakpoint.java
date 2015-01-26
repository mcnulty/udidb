/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.breakpoints;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import net.libudi.api.UdiProcess;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.NoDebuggeeContextException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.ValueResult;

/**
 * An operation to create and install a breakpoint in a debuggee
 *
 * @author mcnulty
 */
@HelpMessage(enMessage="Set a breakpoint in a debuggee")
@LongHelpMessage(enMessage=
        "break <address>\n\n" +
        "Create and install a breakpoint in a debuggee"
)
@DisplayName("break")
public class SetBreakpoint extends DisplayNameOperation {

    // TODO need to add tracking for breakpoints

    private final DebuggeeContext context;

    @Operand(order=0)
    private long address;

    @Inject
    public SetBreakpoint(@Nullable DebuggeeContext context) {
        this.context = context;
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    @Override
    public Result execute() throws OperationException {
        if (context == null) {
            throw new NoDebuggeeContextException();
        }

        try {
            context.getProcess().createBreakpoint(address);

            context.getProcess().installBreakpoint(address);
        }catch (UdiException e) {
            throw new OperationException("Failed to set breakpoint in debuggee", e);
        }

        return new ValueResult(String.format("Set breakpoint at 0x%x", address));
    }
}
