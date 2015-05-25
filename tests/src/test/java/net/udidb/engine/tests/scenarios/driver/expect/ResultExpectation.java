/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver.expect;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.TableRow;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mcnulty
 */
public class ResultExpectation implements OperationResultVisitor
{
    private boolean exceptionExpected = false;

    private boolean deferredExpected = false;

    private boolean voidExpected = false;

    private boolean valueExpected = false;

    private boolean tableExpected = false;

    private Object value = null;

    private List<TableRow> tableRows = null;

    private ValueMatcher valueMatcher = null;

    // Properties used to retrieve the result at a later point
    private Operation op = null;

    private Result result = null;

    private Exception exception = null;

    private ResultExpectation()
    {
    }

    @Override
    public boolean visit(Operation op, VoidResult result)
    {
        this.op = op;
        this.result = result;

        assertTrue(voidExpected);
        return false;
    }

    @Override
    public boolean visit(Operation op, ValueResult result)
    {
        this.op = op;
        this.result = result;

        assertTrue(valueExpected);
        if (value != null) {
            assertEquals(value, result.getValue());
        }
        if (valueMatcher != null) {
            valueMatcher.matches(result.getValue());
        }
        return false;
    }

    @Override
    public boolean visit(Operation op, TableResult result)
    {
        this.op = op;
        this.result = result;

        assertTrue(tableExpected);
        if (tableRows != null) {
            assertEquals(tableRows, result.getRows());
        }
        return false;
    }

    @Override
    public boolean visit(Operation op, DeferredResult result)
    {
        this.op = op;
        this.result = result;

        assertTrue(deferredExpected);
        return false;
    }

    @Override
    public boolean visit(Operation op, Exception e)
    {
        this.op = op;
        this.exception = e;

        assertTrue("Unexpected exception occurred: " + ExceptionUtils.getStackTrace(e), exceptionExpected);
        return false;
    }

    public void accept(OperationResultVisitor visitor)
    {
        if (result != null) {
            result.accept(op, visitor);
        }else{
            visitor.visit(op, exception);
        }
    }

    public static ResultExpectation exception()
    {
        ResultExpectation result = new ResultExpectation();
        result.exceptionExpected = true;
        return result;
    }

    public static ResultExpectation deferred()
    {
        ResultExpectation result = new ResultExpectation();
        result.deferredExpected = true;
        return result;
    }

    public static ResultExpectation expectVoid()
    {
        ResultExpectation result = new ResultExpectation();
        result.voidExpected = true;
        return result;
    }

    public static ResultExpectation value()
    {
        ResultExpectation result = new ResultExpectation();
        result.valueExpected = true;
        result.value = null;
        return result;
    }

    public static ResultExpectation value(ValueMatcher valueMatcher)
    {
        ResultExpectation result = new ResultExpectation();
        result.valueExpected = true;
        result.valueMatcher = valueMatcher;
        return result;
    }

    public static ResultExpectation table()
    {
        ResultExpectation result = new ResultExpectation();
        result.tableExpected = true;
        result.tableRows = null;
        return result;
    }

    public static ResultExpectation table(List<? extends TableRow> tableRows)
    {
        ResultExpectation result = new ResultExpectation();
        result.tableExpected = true;
        result.tableRows = new LinkedList<>(tableRows);
        return result;
    }
}
