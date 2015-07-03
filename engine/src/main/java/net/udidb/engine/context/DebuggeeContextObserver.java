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
 * @author mcnulty
 */
public interface DebuggeeContextObserver
{

    /**
     * @param debuggeeContext the created DebuggeeContext
     */
    void created(DebuggeeContext debuggeeContext);

    /**
     * @param debuggeeContext the deleted DebuggeeContext
     */
    void deleted(DebuggeeContext debuggeeContext);
}
