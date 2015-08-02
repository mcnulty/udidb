/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import net.udidb.server.driver.UdidbServer;

/**
 * @author mcnulty
 */
public abstract class BaseServerTest
{
    private static final String basePath = System.getProperty("native.file.tests.basePath");

    private static final UdidbServer udidbServer = new UdidbServer(new String[]{});

    private final Path binaryPath;
    private final String baseUri;

    protected BaseServerTest(String binaryPath)
    {
        this.binaryPath = Paths.get(basePath, binaryPath);
        this.baseUri = "http://localhost:8888";
    }

    public Path getBinaryPath()
    {
        return binaryPath;
    }

    public String getUri(String uri)
    {
        return baseUri + uri;
    }

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
