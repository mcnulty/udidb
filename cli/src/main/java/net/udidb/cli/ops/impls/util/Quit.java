/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.impls.util;

import com.google.inject.Inject;

import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.VoidResult;

/**
 * An operation to quit the debugger
 *
 * @author mcnulty
 */
@HelpMessage(enMessage="Quit the debugger")
@LongHelpMessage(enMessage="Quit the debugger")
@DisplayName(value ="quit")
public class Quit extends DisplayNameOperation {

    @Inject
    public Quit() {
    }

    @Override
    public Result execute() throws OperationException {
        // Just a no-op operation, the result visitor will need to invoke the quit operation

        return new VoidResult();
    }
}
