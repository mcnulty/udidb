/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.control;

import org.junit.Ignore;
import org.junit.Test;

import net.udidb.engine.tests.scenarios.BaseScenarioTest;
import net.udidb.engine.tests.scenarios.driver.ResultExpectation;
import net.udidb.engine.tests.scenarios.driver.TestCommandBuilder;
import net.udidb.engine.tests.scenarios.driver.TestDriver;

/**
 * Test for breakpoint functionality
 *
 * @author mcnulty
 */
@Ignore
public class BreakpointsTest extends BaseScenarioTest
{
    public BreakpointsTest()
    {
        super("linux/clang/3.2-11/basic-64bit-dynamic");
    }

    @Test
    public void createBreakpoint()
    {
        new TestDriver(
                new TestCommandBuilder()
                        .setResultExpectation(ResultExpectation.expectVoid())
                        .createDebuggee(getBinaryPath().toAbsolutePath().toString()),
                new TestCommandBuilder()
                        .setResultExpectation(ResultExpectation.value())
                        .setBreakpoint("main")
        ).runTest();
    }
}
