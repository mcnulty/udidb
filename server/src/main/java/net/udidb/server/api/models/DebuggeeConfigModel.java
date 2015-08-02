/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mcnulty
 */
public class DebuggeeConfigModel
{
    private String execPath;

    private List<String> args;

    private Map<String, String> env;

    public List<String> getArgs()
    {
        return args;
    }

    public void setArgs(List<String> args)
    {
        this.args = args;
    }

    public Map<String, String> getEnv()
    {
        return env;
    }

    public void setEnv(Map<String, String> env)
    {
        this.env = env;
    }

    public String getExecPath()
    {
        return execPath;
    }

    public void setExecPath(String execPath)
    {
        this.execPath = execPath;
    }

    public OperationModel createOperationModel()
    {
        OperationModel operationModel = new OperationModel();
        operationModel.setName("create");

        Map<String, Object> operands = new HashMap<>();
        operands.put("execPath", execPath);
        if (args != null) {
            operands.put("args", args);
        }else{
            operands.put("args", Collections.emptyList());
        }
        operationModel.setOperands(operands);

        return operationModel;
    }
}
