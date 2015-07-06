/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import java.util.Map;

import net.udidb.engine.ops.results.Result;

/**
 * @author mcnulty
 */
public final class OperationModel
{
    private String name;

    private Map<String, Object> operands;

    private boolean pending;

    private Result result;

    public OperationModel()
    {
    }

    public OperationModel(OperationModel operationModel)
    {
        this.name = operationModel.name;
        this.operands = operationModel.operands;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, Object> getOperands()
    {
        return operands;
    }

    public void setOperands(Map<String, Object> operands)
    {
        this.operands = operands;
    }

    public boolean isPending()
    {
        return pending;
    }

    public void setPending(boolean pending)
    {
        this.pending = pending;
    }

    public Result getResult()
    {
        return result;
    }

    public void setResult(Result result)
    {
        this.result = result;
    }
}
