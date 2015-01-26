/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.impls.internals;

import com.google.inject.Inject;

import net.udidb.cli.ops.CliResultVisitor;
import net.udidb.cli.ops.events.CliEventDispatcher;
import net.udidb.cli.ops.events.CliEventVisitor;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.impls.SetterOperation;
import net.udidb.engine.ops.impls.Setting;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.VoidResult;

/**
 * Operation to control whether stack traces are printed for exceptions
 *
 * @author mcnulty
 */
@HelpMessage(enMessage = "Disable/enable the display of all events")
@LongHelpMessage(enMessage=
        "internals display-all-events <boolean>\n\n" +
        "Disable/enable the display of all events, including the intermediate events from a complex operation"
)
@DisplayName("internals display-all-events")
public class SetDisplayAllEvents extends SetterOperation<Boolean> implements Setting {

    private final CliEventVisitor cliEventVisitor;

    @Inject
    public SetDisplayAllEvents(CliEventVisitor cliEventVisitor) {
        this.cliEventVisitor = cliEventVisitor;
    }

    @Override
    public Result execute() throws OperationException {
        cliEventVisitor.setDisplayAllEvents(value);

        return new VoidResult();
    }

    @Override
    public Object getSetting() {
        return cliEventVisitor.isDisplayAllEvents();
    }
}
