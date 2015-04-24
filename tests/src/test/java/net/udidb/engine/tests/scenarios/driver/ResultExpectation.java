/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver;

import java.util.LinkedList;
import java.util.List;

import net.udidb.engine.ops.results.TableRow;

/**
 * @author mcnulty
 */
public class ResultExpectation
{
    private boolean exceptionExpected = false;

    private boolean deferredExpected = false;

    private boolean voidExpected = false;

    private boolean valueExpected = false;

    private boolean tableExpected = false;

    private Object value = null;

    private List<TableRow> tableRows = null;

    private ResultExpectation()
    {
    }

    public boolean isDeferredExpected()
    {
        return deferredExpected;
    }

    public boolean isExceptionExpected()
    {
        return exceptionExpected;
    }

    public boolean isTableExpected()
    {
        return tableExpected;
    }

    public boolean isValueExpected()
    {
        return valueExpected;
    }

    public List<TableRow> getTableRows()
    {
        return tableRows;
    }

    public Object getValue()
    {
        return value;
    }

    public boolean isVoidExpected()
    {
        return voidExpected;
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

    public static ResultExpectation value(Object expectedValue)
    {
        ResultExpectation result = new ResultExpectation();
        result.valueExpected = true;
        result.value = expectedValue;
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
