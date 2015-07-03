/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver;

import java.io.IOException;

import com.google.inject.Inject;

import net.udidb.cli.driver.GlobalContextManager;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.cli.ops.OperationReader;
import net.udidb.engine.ops.UnknownOperationException;
import net.udidb.engine.ops.parser.OperationParser;
import net.udidb.engine.ops.parser.ParsingContext;

/**
 * @author mcnulty
 */
public class TestOperationReader implements OperationReader
{
    private final OperationParser operationParser;
    private final GlobalContextManager globalContextManager;

    private String currentCommandString = null;

    public String getCurrentCommandString()
    {
        return currentCommandString;
    }

    public void setCurrentCommandString(String currentCommandString)
    {
        this.currentCommandString = currentCommandString;
    }

    @Inject
    TestOperationReader(OperationParser operationParser, GlobalContextManager globalContextManager)
    {
        this.operationParser = operationParser;
        this.globalContextManager = globalContextManager;
    }

    @Override
    public Operation read() throws IOException, UnknownOperationException, OperationParseException
    {
        ParsingContext parsingContext = new ParsingContext();
        parsingContext.setDebuggeeContext(globalContextManager.getCurrent());
        return operationParser.parse(currentCommandString, parsingContext);
    }
}
