/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser.ut;

import java.util.List;

import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.results.Result;

/**
 * @author mcnulty
 */
@DisplayName("test")
public class TestOp extends DisplayNameOperation
{
    @Operand(order = 0, restOfLine = true)
    private List<String> args;

    public List<String> getArgs()
    {
        return args;
    }

    public void setArgs(List<String> args)
    {
        this.args = args;
    }

    @Override
    public Result execute() throws OperationException
    {
        return null;
    }
}
