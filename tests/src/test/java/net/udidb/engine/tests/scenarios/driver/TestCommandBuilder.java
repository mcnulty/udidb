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
 * A builder for common TestCommand objects
 *
 * @author mcnulty
 */
public final class TestCommandBuilder
{
    private ResultExpectation resultExpectation;

    private EventType eventType;

    public TestCommandBuilder()
    {
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public TestCommandBuilder setEventType(EventType eventType)
    {
        this.eventType = eventType;
        return this;
    }

    public ResultExpectation getResultExpectation()
    {
        return resultExpectation;
    }

    public TestCommandBuilder setResultExpectation(ResultExpectation resultExpectation)
    {
        this.resultExpectation = resultExpectation;
        return this;
    }

    public TestCommand createDebuggee(String executablePath)
    {
        return new TestCommand("create " + executablePath, resultExpectation, eventType);
    }

    public TestCommand setBreakpoint(String expression)
    {
        return new TestCommand("break " + expression, resultExpectation, eventType);
    }
}
