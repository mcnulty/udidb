/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver;

import net.libudi.api.event.EventType;

/**
 * @author mcnulty
 */
public class TestCommand
{
    private final String commandString;

    private final ResultExpectation resultExpectation;

    private final EventType expectedEventType;

    public TestCommand(String commandString, ResultExpectation resultExpectation)
    {
        this.commandString = commandString;
        this.resultExpectation = resultExpectation;
        this.expectedEventType = null;
    }

    public TestCommand(String commandString, ResultExpectation resultExpectation, EventType expectedEventType)
    {
        this.commandString = commandString;
        this.resultExpectation = resultExpectation;
        this.expectedEventType = expectedEventType;
    }

    public String getCommandString()
    {
        return commandString;
    }

    public EventType getExpectedEventType()
    {
        return expectedEventType;
    }

    public ResultExpectation getResultExpectation()
    {
        return resultExpectation;
    }
}
