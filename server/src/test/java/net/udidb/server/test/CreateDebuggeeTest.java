/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import net.libudi.api.event.EventType;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.ProcessModel;
import net.udidb.server.api.models.UdiEventModel;

public class CreateDebuggeeTest extends BaseServerTest
{
    public CreateDebuggeeTest()
    {
        super("basic-debug-noopt-dynamic");
    }

    @Test
    public void testCreateDebuggee() throws Exception
    {
        DebuggeeContextModel contextModel = createDebuggee();

        ProcessModel processModel = given()
                .when()
                .get(getUri(String.format("/debuggeeContexts/%s/process", contextModel.getId())))
                .as(ProcessModel.class);
        assertTrue(processModel.getPid() != null);

        continueContext(contextModel);

        waitForExit(contextModel, 0);

        continueContext(contextModel);

        UdiEventModel event = waitForEvent();
        assertNotNull(event);
        assertEquals(EventType.PROCESS_CLEANUP, event.getEventType());
    }
}
