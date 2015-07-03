/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.impls.display.show;

import com.google.inject.Inject;

import net.udidb.cli.driver.GlobalContextManager;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;

/**
 * Operation to display the all the managed DebuggeeContexts
 *
 * @author mcnulty
 */
@HelpMessage(enMessage = "Display all debuggee contexts")
@LongHelpMessage(enMessage =
        "show contexts\n\n" +
        "Show all debuggee contexts managed by the debugger"
)
@DisplayName("show contexts")
public class Contexts extends DisplayNameOperation {

    private final GlobalContextManager contextManager;

    @Inject
    public Contexts(GlobalContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public Result execute() throws OperationException {
        return new TableResult(contextManager.getContextRows());
    }
}
