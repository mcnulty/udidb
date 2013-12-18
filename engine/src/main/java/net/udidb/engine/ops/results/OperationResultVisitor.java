/*
 * Copyright (c) 2011-2013, Dan McNulty
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the UDI project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
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
