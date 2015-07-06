/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import net.udidb.engine.ops.OperationException;

/**
 * @author mcnulty
 */
public class OperationInProgressException extends OperationException
{

    public OperationInProgressException(Throwable cause)
    {
        super(cause);
    }

    public OperationInProgressException(String message)
    {
        super(message);
    }

    public OperationInProgressException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
