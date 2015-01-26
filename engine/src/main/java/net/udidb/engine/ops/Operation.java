/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops;

import net.udidb.engine.ops.results.Result;

/**
 * The unit of action taken by the debugger
 *
 * @author dmcnulty
 */
public interface Operation {

    /**
     * @return the value of the operation
     */
    String getName();

    /**
     * Executes this operation
     *
     * @return the result of the operation
     *
     * @throws OperationException on error to execute the operation
     */
    Result execute() throws OperationException;
}
