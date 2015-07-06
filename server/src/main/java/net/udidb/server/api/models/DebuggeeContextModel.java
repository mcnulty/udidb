/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.udidb.engine.context.DebuggeeContext;

/**
 * @author mcnulty
 */
public final class DebuggeeContextModel
{
    private String id;

    private String execPath;

    private List<String> args;

    private int pid;

    public DebuggeeContextModel()
    {
    }

    public DebuggeeContextModel(DebuggeeContext debuggeeContext)
    {
        this.id = debuggeeContext.getId();
        this.execPath = debuggeeContext.getExecPath().toAbsolutePath().toString();
        this.args = new ArrayList<>(Arrays.asList(debuggeeContext.getArgs()));
        this.pid = debuggeeContext.getProcess().getPid();
    }

    public List<String> getArgs()
    {
        return args;
    }

    public void setArgs(List<String> args)
    {
        this.args = args;
    }

    public String getExecPath()
    {
        return execPath;
    }

    public void setExecPath(String execPath)
    {
        this.execPath = execPath;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }
}
