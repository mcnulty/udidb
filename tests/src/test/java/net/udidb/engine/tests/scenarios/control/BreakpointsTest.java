/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.control;

import org.junit.Test;

import net.udidb.engine.tests.scenarios.BaseScenarioTest;
import net.udidb.engine.tests.scenarios.driver.TestCommand;
import net.udidb.engine.tests.scenarios.driver.expect.CommandValueEquality;
import net.udidb.engine.tests.scenarios.driver.expect.ResultExpectation;
import net.udidb.engine.tests.scenarios.driver.TestCommandBuilder;
import net.udidb.engine.tests.scenarios.driver.TestDriver;

/**
 * Test for breakpoint functionality
 *
 * @author mcnulty
 */
public class BreakpointsTest extends BaseScenarioTest
{
    public BreakpointsTest()
    {
        super("basic");
    }

    @Test
    public void createBreakpointAtMain()
    {
        TestCommand create = new TestCommandBuilder()
                .setResultExpectation(ResultExpectation.value())
                .createDebuggee(getBinaryPath().toAbsolutePath().toString());
        TestCommand getAddress = new TestCommandBuilder()
                .setResultExpectation(ResultExpectation.value())
                .eval("main");
        TestCommand setBreakpoint = new TestCommandBuilder()
                        .setResultExpectation(ResultExpectation.value(new CommandValueEquality(getAddress)))
                        .setBreakpoint("main");

        new TestDriver(create, getAddress, setBreakpoint).runTest();
    }
}
