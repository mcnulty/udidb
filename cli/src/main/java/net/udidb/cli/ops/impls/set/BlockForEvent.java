/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.impls.set;

import com.google.inject.Inject;

import net.udidb.cli.ops.events.CliEventDispatcher;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.impls.SetterOperation;
import net.udidb.engine.ops.impls.Setting;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.VoidResult;

/**
 * Sets the block-for-event configuration property
 *
 * @author mcnulty
 */
@HelpMessage("Disable/enable whether to block for an event after continuing a debuggee")
@DisplayName("set block-for-event")
public class BlockForEvent extends SetterOperation<Boolean> implements Setting {

    private final CliEventDispatcher eventDispatcher;

    @Inject
    public BlockForEvent(CliEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public Result execute() throws OperationException {
        eventDispatcher.setBlockForEvent(value);

        return new VoidResult();
    }

    @Override
    public Object getSetting() {
        return eventDispatcher.isBlockForEvent();
    }
}
