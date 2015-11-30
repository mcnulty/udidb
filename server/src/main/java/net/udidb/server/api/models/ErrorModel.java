/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

/**
 * @author mcnulty
 */
public class ErrorModel
{
    private String message;

    private String exceptionName;

    public ErrorModel()
    {
    }

    public ErrorModel(Exception e)
    {
        this.message = e.getMessage();
        this.exceptionName = e.getClass().getName();
    }

    public String getExceptionName()
    {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName)
    {
        this.exceptionName = exceptionName;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
