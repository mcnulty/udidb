/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops;

/**
 * An exception indicating that debug info is missing and the requested operation cannot be completed
 *
 * @author mcnulty
 */
public class MissingDebugInfoException extends OperationException {

    public MissingDebugInfoException() {
        super("Debug information unavailable, cannot execute operation");
    }

    public MissingDebugInfoException(Throwable cause) {
        super(cause);
    }
}
