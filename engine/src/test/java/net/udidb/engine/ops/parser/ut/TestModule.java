/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser.ut;

import com.google.inject.name.Names;

import net.udidb.engine.ops.parser.ParserModule;

/**
 * @author mcnulty
 */
public class TestModule extends ParserModule
{
    @Override
    protected void configure()
    {
        super.configure();
        bind(String[].class).annotatedWith(Names.named("CUSTOM_IMPL_PACKAGES")).toInstance(
                new String[]{ "net.udidb.engine.ops.parser.ut" });
    }
}
