/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;

import net.sourcecrumbs.file.tests.NativeFileTestsInfo;

/**
 * @author mcnulty
 */
public abstract class BaseScenarioTest
{
    private static final Path basePath = Paths.get(System.getProperty("native.file.tests.basePath"));
    private static NativeFileTestsInfo fileTestsInfo = null;

    private final Path binaryPath;

    protected BaseScenarioTest(String url)
    {
        binaryPath = fileTestsInfo.getFirstExecutablePath(url);
    }

    @BeforeClass
    public static void initClass() throws IOException
    {
        fileTestsInfo = new NativeFileTestsInfo(basePath);
    }

    public Path getBinaryPath()
    {
        return binaryPath;
    }
}
