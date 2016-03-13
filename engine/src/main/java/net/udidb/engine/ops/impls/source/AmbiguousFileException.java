/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.source;

import net.udidb.engine.ops.OperationException;

/**
 * Thrown when a file is requested but it could refer to multiple files
 *
 * @author mcnulty
 */
public class AmbiguousFileException extends OperationException
{
    public AmbiguousFileException(String file)
    {
        super("Could not find unique file that matches path of " + file);
    }

}
