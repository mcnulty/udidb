/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

/**
 * Explicit singleton for the ServerEngine to allow the instance to be referenced by the REST API whose object lifetimes
 * are managed by Jersey.
 *
 * @author mcnulty
 */
public enum ServerEngine
{
    INSTANCE;

    private ServerEngine()
    {

    }
}
