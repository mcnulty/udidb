/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.context;

/**
 * Provides a mechanism to inform a component of the current debuggee context
 *
 * @author mcnulty
 */
public interface DebuggeeContextAware
{

    /**
     * @param debuggeeContext the debuggeeContext to set
     */
    void setDebuggeeContext(DebuggeeContext debuggeeContext);

}
