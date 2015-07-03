/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.driver;

import java.util.LinkedList;
import java.util.List;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.results.TableRow;

/**
 * An Adapter of DebuggeeContext that displays the active debuggee context
 *
 * @author mcnulty
 */
public class ActiveDebuggeeContextAdapter implements TableRow
{
    private final DebuggeeContext context;
    private final boolean active;

    public ActiveDebuggeeContextAdapter(DebuggeeContext context, boolean active)
    {
        this.context = context;
        this.active = active;
    }

    @Override
    public List<String> getColumnHeaders()
    {
        List<String> result = new LinkedList<>();
        result.add("Active");
        result.addAll(context.getColumnHeaders());
        return result;
    }

    @Override
    public List<String> getColumnValues()
    {
        List<String> result = new LinkedList<>();
        result.add(active ? "*" : "");
        result.addAll(context.getColumnValues());
        return result;
    }
}
