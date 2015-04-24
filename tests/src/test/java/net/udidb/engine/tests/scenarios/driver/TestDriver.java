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
import java.util.Queue;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.emory.mathcs.backport.java.util.Arrays;
import net.udidb.engine.UdidbEngine;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.impls.util.Quit;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mcnulty
 */
public class TestDriver implements OperationResultVisitor
{
    private final Injector injector;
    private final TestConfig config;
    private final TestOperationReader operationReader;
    private final EventDispatcher eventDispatcher;
    private final TestEventVisitor eventVisitor;

    private final Queue<TestCommand> testCommands;
    private TestCommand currentCommand;

    public TestDriver(TestCommand... testCommands)
    {
        this(Arrays.asList(testCommands));
    }

    public TestDriver(List<TestCommand> testCommands)
    {
        this.injector = Guice.createInjector(new ScenariosModule());
        this.config = injector.getInstance(TestConfig.class);
        this.operationReader = injector.getInstance(TestOperationReader.class);
        this.eventDispatcher = injector.getInstance(EventDispatcher.class);
        this.eventVisitor = injector.getInstance(TestEventVisitor.class);

        this.testCommands = new LinkedList<>(testCommands);
        this.testCommands.add(new TestCommand("quit", ResultExpectation.expectVoid()));

        // set up the first command to execute
        this.currentCommand = this.testCommands.remove();
        this.operationReader.setCurrentCommandString(this.currentCommand.getCommandString());
    }

    public void runTest()
    {
        UdidbEngine engine = new UdidbEngine(config, operationReader, this, eventDispatcher);
        engine.run();
    }

    @Override
    public boolean visit(Operation op, VoidResult result)
    {
        if (op instanceof Quit) {
            return false;
        }

        assertTrue(currentCommand.getResultExpectation().isVoidExpected());
        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, ValueResult result)
    {
        assertTrue(currentCommand.getResultExpectation().isValueExpected());

        if (currentCommand.getResultExpectation().getValue() != null) {
            assertEquals(currentCommand.getResultExpectation().getValue(), result.getValue());
        }

        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, TableResult result)
    {
        assertTrue(currentCommand.getResultExpectation().isTableExpected());

        if (currentCommand.getResultExpectation().getTableRows() != null) {
            assertEquals(currentCommand.getResultExpectation().getTableRows(), result.getRows());
        }

        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, DeferredResult result)
    {
        assertTrue(currentCommand.getResultExpectation().isDeferredExpected());
        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, Exception e)
    {
        if (!currentCommand.getResultExpectation().isExceptionExpected()) {
            e.printStackTrace();
        }

        assertTrue(currentCommand.getResultExpectation().isExceptionExpected());
        return advanceCommand();
    }

    private boolean advanceCommand()
    {
        eventVisitor.setCurrentExpectedEventType(currentCommand.getExpectedEventType());

        if (!testCommands.isEmpty()) {
            currentCommand = testCommands.remove();
            operationReader.setCurrentCommandString(currentCommand.getCommandString());
        }

        return true;
    }
}
