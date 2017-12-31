/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import net.libudi.api.UdiProcess;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.ops.OperationException;

/**
 * @author mcnulty
 */
public final class ProcessModel
{
    private String pid;

    private boolean running;

    public ProcessModel()
    {
    }

    public ProcessModel(UdiProcess udiProcess) throws OperationException
    {
        try {
            this.pid = Integer.toString(udiProcess.getPid());
            this.running = udiProcess.isRunning();
        } catch (UdiException e) {
            throw new OperationException(e);
        }
    }

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public boolean isRunning()
    {
        return running;
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }
}
