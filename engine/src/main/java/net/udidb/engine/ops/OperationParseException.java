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
 * If there is an error parsing the operation
 *
 * @author mcnulty
 */
public class OperationParseException extends OperationException {

    private static final long serialVersionUID = -3103066208013724417L;

    /**
     * Constructor.
     *
     * @param message the message
     */
    public OperationParseException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public OperationParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message the message
     * @param cause the cause
     */
    public OperationParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
