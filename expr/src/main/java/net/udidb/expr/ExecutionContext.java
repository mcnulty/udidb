/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr;

import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.files.Executable;

/**
 * Provides access to various pieces of data from the execution context under which evaluation is taking place
 *
 * @author mcnulty
 */
public interface ExecutionContext
{

    /**
     * @return the current (executing) thread
     */
    UdiThread getCurrentThread();

    /**
     * @return the Executable handle
     */
    Executable getExecutable();
}
