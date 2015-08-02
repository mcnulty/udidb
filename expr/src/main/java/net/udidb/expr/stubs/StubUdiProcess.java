/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.stubs;

import java.util.List;

import net.libudi.api.Architecture;
import net.libudi.api.UdiProcess;
import net.libudi.api.UdiThread;
import net.libudi.api.event.EventType;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.exceptions.UdiException;
import net.libudi.api.exceptions.UnexpectedEventException;

/**
 * @author mcnulty
 */
public class StubUdiProcess implements UdiProcess
{

    @Override
    public int getPid()
    {
        return 0;
    }

    @Override
    public Architecture getArchitecture()
    {
        return Architecture.X86_64;
    }

    @Override
    public boolean isMultithreadCapable()
    {
        return false;
    }

    @Override
    public UdiThread getInitialThread()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRunning()
    {
        return false;
    }

    @Override
    public boolean isTerminated()
    {
        return false;
    }

    @Override
    public boolean isWaitingForStart()
    {
        return true;
    }

    @Override
    public void continueProcess() throws UdiException
    {

    }

    @Override
    public void refreshState() throws UdiException
    {

    }

    @Override
    public void readMemory(byte[] data, long sourceAddr) throws UdiException
    {

    }

    @Override
    public void writeMemory(byte[] data, long destAddr) throws UdiException
    {

    }

    @Override
    public void createBreakpoint(long brkptAddr) throws UdiException
    {

    }

    @Override
    public void installBreakpoint(long brkptAddr) throws UdiException
    {

    }

    @Override
    public void removeBreakpoint(long brkptAddr) throws UdiException
    {

    }

    @Override
    public void deleteBreakpoint(long brkptAddr) throws UdiException
    {

    }

    @Override
    public List<UdiEvent> waitForEvents() throws UdiException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public UdiEvent waitForEvent(EventType eventType) throws UnexpectedEventException, UdiException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getUserData()
    {
        return null;
    }

    @Override
    public void setUserData(Object object)
    {

    }

    @Override
    public void close() throws Exception
    {

    }
}
