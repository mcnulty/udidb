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
 * Base exception for all errors encountered during operation processing
 *
 * @author mcnulty
 */
public class OperationException extends Exception {

    /** auto-generated serial version UID */
    private static final long serialVersionUID = 750345500850969447L;

    /**
     * Constructor.
     *
     * @param message the message
     */
    public OperationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public OperationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message the message
     * @param cause the cause
     */
    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
