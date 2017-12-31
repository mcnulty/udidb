/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.libudi.api.event.EventType;
import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.UdiEventModel;

import static org.junit.Assert.assertEquals;

/**
 * Test for breakpoint functionality
 */
public class BreakpointsTest extends BaseServerTest
{
    public BreakpointsTest()
    {
        super("basic-debug-noopt-dynamic");
    }

    @Test
    public void testBreakpointAtMain() throws Exception
    {
        DebuggeeContextModel contextModel = createDebuggee();

        OperationModel evalOp = new OperationModel();
        evalOp.setName("eval");
        evalOp.setOperands(ImmutableMap.of("expression", Lists.newArrayList("main")));

        submitOperation(contextModel, evalOp).statusCode(200);

        OperationModel breakOp = new OperationModel();
        breakOp.setName("break");
        breakOp.setOperands(ImmutableMap.of("expression", Lists.newArrayList("main")));

        submitOperation(contextModel, breakOp).statusCode(200);

        continueContext(contextModel);

        UdiEventModel event = waitForEvent();

        assertEquals(EventType.BREAKPOINT, event.getEventType());

        continueContext(contextModel);

        waitForExit(contextModel, 0);

        continueContext(contextModel);
    }
}
