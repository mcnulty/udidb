/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.stubs;

import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.files.Executable;
import net.udidb.expr.ExecutionContext;

/**
 * @author mcnulty
 */
public class StubExecutionContext implements ExecutionContext
{
    private final UdiThread stubThread = new StubUdiThread();

    private final Executable stubExecutable = new StubExecutable();

    @Override
    public UdiThread getCurrentThread()
    {
        return stubThread;
    }

    @Override
    public Executable getExecutable()
    {
        return stubExecutable;
    }
}
