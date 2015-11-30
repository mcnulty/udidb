/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import net.libudi.api.UdiThread;
import net.libudi.api.exceptions.UdiException;

/**
 * @author mcnulty
 */
public final class ThreadModel
{
    private String id;
    private String pc;

    public ThreadModel()
    {
    }

    public ThreadModel(UdiThread udiThread) throws UdiException
    {
        this.id = Long.toString(udiThread.getTid());
        this.pc = Long.toHexString(udiThread.getPC());
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getPc()
    {
        return pc;
    }

    public void setPc(String pc)
    {
        this.pc = pc;
    }
}
