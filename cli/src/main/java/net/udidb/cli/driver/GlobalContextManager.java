/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.driver;

import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.context.DebuggeeContextObserver;
import net.udidb.engine.ops.results.TableRow;

/**
 * A class that maintains the current global DebuggeeContext that is passed to control Operations
 *
 * @author mcnulty
 */
@Singleton
public class GlobalContextManager implements DebuggeeContextObserver
{
    private final DebuggeeContextManager debuggeeContextManager;

    private DebuggeeContext current = null;

    @Inject
    GlobalContextManager(DebuggeeContextManager debuggeeContextManager)
    {
        this.debuggeeContextManager = debuggeeContextManager;
        this.debuggeeContextManager.addObserver(this);
    }

    public DebuggeeContext getCurrent() {
        return current;
    }

    public void setCurrent(DebuggeeContext current) {
        this.current = current;
    }

    public List<TableRow> getContextRows() {

        return debuggeeContextManager.getContexts().values().stream()
                .map(c -> new ActiveDebuggeeContextAdapter(c, c == current))
                .collect(Collectors.toList());
    }

    @Override
    public void created(DebuggeeContext debuggeeContext)
    {
        setCurrent(debuggeeContext);
    }

    @Override
    public void deleted(DebuggeeContext debuggeeContext)
    {
        if (current == debuggeeContext) {
            current = null;
        }
    }
}
