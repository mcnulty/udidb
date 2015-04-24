/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mcnulty
 */
public abstract class BaseScenarioTest
{
    private static final String basePath = System.getProperty("native.file.tests.basePath");

    private final Path binaryPath;

    protected BaseScenarioTest(String url)
    {
        binaryPath = Paths.get(basePath, url);
    }

    public Path getBinaryPath()
    {
        return binaryPath;
    }
}
