/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver.expect;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;
import net.udidb.engine.tests.scenarios.driver.TestCommand;

import static org.junit.Assert.assertNotNull;

/**
 * Matches a value output from a previous command
 *
 * @author mcnulty
 */
public abstract class BaseCommandValueMatcher implements ValueMatcher, OperationResultVisitor
{
    private final TestCommand command;

    private ValueResult result = null;

    protected BaseCommandValueMatcher(TestCommand command)
    {
        this.command = command;
    }

    @Override
    public void matches(Object value) throws AssertionError
    {
        // Obtain the value result
        ResultExpectation resultExpectation = command.getResultExpectation();
        assertNotNull("Result expectation from command '" + command + "' unavailable", resultExpectation);

        resultExpectation.accept(this);

        assertNotNull("Result from command '" + command + "' unavailable or not a value", result);

        matches(result, value);
    }

    protected abstract void matches(ValueResult result, Object value) throws AssertionError;

    @Override
    public boolean visit(Operation op, VoidResult result)
    {
        return false;
    }

    @Override
    public boolean visit(Operation op, ValueResult result)
    {
        this.result = result;
        return false;
    }

    @Override
    public boolean visit(Operation op, TableResult result)
    {
        return false;
    }

    @Override
    public boolean visit(Operation op, DeferredResult result)
    {
        return false;
    }

    @Override
    public boolean visit(Operation op, Exception e)
    {
        return false;
    }
}
