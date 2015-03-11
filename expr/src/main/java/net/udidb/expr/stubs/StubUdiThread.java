/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.stubs;

import net.libudi.api.Register;
import net.libudi.api.ThreadState;
import net.libudi.api.UdiProcess;
import net.libudi.api.UdiThread;
import net.libudi.api.exceptions.UdiException;

/**
 * @author mcnulty
 */
public class StubUdiThread implements UdiThread
{
    private final StubUdiProcess process = new StubUdiProcess();

    @Override
    public long getTid()
    {
        return 0;
    }

    @Override
    public UdiProcess getParentProcess()
    {
        return process;
    }

    @Override
    public ThreadState getState()
    {
        return ThreadState.SUSPENDED;
    }

    @Override
    public UdiThread getNextThread()
    {
        return null;
    }

    @Override
    public long getPC() throws UdiException
    {
        return 0;
    }

    @Override
    public long getNextPC() throws UdiException
    {
        return 0;
    }

    @Override
    public long readRegister(Register reg) throws UdiException
    {
        return 0;
    }

    @Override
    public void writeRegister(Register reg, long value) throws UdiException
    {

    }

    @Override
    public void resume() throws UdiException
    {

    }

    @Override
    public void suspend() throws UdiException
    {

    }

    @Override
    public void setSingleStep(boolean singleStep) throws UdiException
    {

    }

    @Override
    public boolean getSingleStep()
    {
        return false;
    }
}
