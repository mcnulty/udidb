/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.results;

import net.udidb.engine.ops.Operation;

/**
 * Visitor used to process a result of an operation
 *
 * @author mcnulty
 */
public interface OperationResultVisitor {

    /**
     * Processes the VoidResult of the specified operation
     *
     * @param op the operation
     * @param result the VoidResult of the operation
     *
     * @return true, if the result indicates further operations should be executed; false otherwise
     */
    boolean visit(Operation op, VoidResult result);

    /**
     * Processes the ValueResult of the specified operation
     *
     * @param op the operation
     * @param result the ValueResult of the operation
     *
     * @return true, if the result indicates further operations should be executed; false otherwise
     */
    boolean visit(Operation op, ValueResult result);

    /**
     * Processes the TableResult of the specified operation
     *
     * @param op the operation
     * @param result the TableResult of the operation
     *
     * @return true, if the result indicates further operations should be executed; false otherwise
     */
    boolean visit(Operation op, TableResult result);

    /**
     * Processes the DeferredResult of the specified operation
     *
     * @param op the operation
     * @param result the DeferredResult of the operation
     *
     * @return true, if the result indicates further operations should be executed; false otherwise
     */
    boolean visit(Operation op, DeferredResult result);

    /**
     * Processes the exception that occurred while executing or parsing the operation
     *
     * @param op the operation or null if the operation could not be determined
     * @param e the exception
     *
     * @return true, if further operations should be executed; false otherwise
     */
    boolean visit(Operation op, Exception e);
}
