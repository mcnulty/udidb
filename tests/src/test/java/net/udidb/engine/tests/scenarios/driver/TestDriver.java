/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.udidb.cli.driver.CliEngine;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.impls.util.Quit;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;
import net.udidb.engine.tests.scenarios.driver.expect.ResultExpectation;

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
        CliEngine engine = new CliEngine(config, operationReader, this, eventDispatcher);
        engine.run();
    }

    @Override
    public boolean visit(Operation op, VoidResult result)
    {
        if (op instanceof Quit) {
            return false;
        }

        currentCommand.getResultExpectation().visit(op, result);
        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, ValueResult result)
    {
        currentCommand.getResultExpectation().visit(op, result);
        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, TableResult result)
    {
        currentCommand.getResultExpectation().visit(op, result);
        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, DeferredResult result)
    {
        currentCommand.getResultExpectation().visit(op, result);
        return advanceCommand();
    }

    @Override
    public boolean visit(Operation op, Exception e)
    {
        currentCommand.getResultExpectation().visit(op, e);
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
