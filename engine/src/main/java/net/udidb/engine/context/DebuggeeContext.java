/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.context;

import java.nio.file.Path;
import java.util.Map;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessConfig;
import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.debug.symbols.RegisterContext;
import net.sourcecrumbs.api.files.Executable;
import net.udidb.engine.ops.results.TableRow;
import net.udidb.expr.ExecutionContext;

/**
 * An interface exposing access to all configuration and state for a specific debuggee
 *
 * @author mcnulty
 */
public interface DebuggeeContext extends TableRow, RegisterContext, ExecutionContext
{
    String getId();

    Path getRootDir();

    Map<String, String> getEnv();

    Path getExecPath();

    String[] getArgs();

    UdiProcess getProcess();

    Object getEngineUserData();

    void setEngineUserData(Object engineUserData);
}
