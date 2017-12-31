/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.udidb.server.driver.UdidbServer;

@RunWith(Suite.class)
@SuiteClasses({
        CreateDebuggeeTest.class,
        BreakpointsTest.class
})
public class ServerTestSuite
{
    private static final UdidbServer udidbServer = new UdidbServer(new String[]{});

    @BeforeClass
    public static void startServer() throws Exception
    {
        udidbServer.start();
    }

    @AfterClass
    public static void stopServer() throws Exception
    {
        udidbServer.stop();
    }
}
