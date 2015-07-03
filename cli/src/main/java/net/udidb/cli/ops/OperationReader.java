/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops;

import java.io.IOException;
import java.util.Map;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.UnknownOperationException;

/**
 * Provides a mechanism to wait for operations to be specified to the debugger
 *
 * @author mcnulty
 */
public interface OperationReader {

    /**
     * Blocks until the next operation is available
     *
     * @return the operation
     *
     * @throws IOException on failure to read the operation
     * @throws UnknownOperationException when an unknown operation is specified
     * @throws OperationParseException when the operation cannot be parsed from the input
     */
    Operation read() throws IOException, UnknownOperationException, OperationParseException;
}
