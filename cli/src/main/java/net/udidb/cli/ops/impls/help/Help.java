/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.impls.help;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.ValueResult;

/**
 * Display help messages for operations
 *
 * @author mcnulty
 */
@HelpMessage(enMessage = "Display help for operations")
@LongHelpMessage(enMessage =
        "help [operation name]\n\n" +
        "Display detailed help for a specific operation or short help for all operations"
)
@DisplayName("help")
public class Help extends DisplayNameOperation {

    private final HelpMessageProvider provider;

    @Operand(order=0, optional=true, restOfLine=true)
    private List<String> args;

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    @Inject
    public Help(HelpMessageProvider provider) {
        this.provider = provider;
    }

    @Override
    public Result execute() throws OperationException {
        if (args == null) {
            StringBuilder builder = new StringBuilder();
            provider.getAllShortMessages(builder);

            return new ValueResult(builder.toString());
        }

        String opName = StringUtils.join(args, " ");

        String longMessage = provider.getLongMessage(opName);
        if (longMessage == null) {
            throw new OperationException(String.format("No help available for operation '%s'", opName));
        }

        return new ValueResult(longMessage);
    }
}
