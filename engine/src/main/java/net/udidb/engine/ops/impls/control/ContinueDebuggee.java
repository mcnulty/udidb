/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.control;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.NoDebuggeeContextException;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.VoidResult;

/**
 * Operation to continue the debuggee
 *
 * @author mcnulty
 */
@HelpMessage(enMessage="Continue a debuggee")
@LongHelpMessage(enMessage=
        "continue\n\n" +
        "Continue a debuggee"
)
@DisplayName("continue")
public class ContinueDebuggee extends DisplayNameOperation {

    private final DebuggeeContext context;

    @Inject
    public ContinueDebuggee(@Nullable DebuggeeContext context) {
        this.context = context;
    }

    @Override
    public Result execute() throws OperationException {
        if (context == null) {
            throw new NoDebuggeeContextException();
        }

        try {
            context.getProcess().continueProcess();
        }catch (UdiException e) {
            throw new OperationException("Failed to continue debuggee", e);
        }

        // The process runs until an event occurs
        return new VoidResult(true);
    }
}
