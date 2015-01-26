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
 * @author mcnulty
 */
public class UnknownOperationException extends OperationException {

    /**
     * Constructor.
     *
     * @param message the message
     */
    public UnknownOperationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public UnknownOperationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnknownOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
