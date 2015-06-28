/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser.ut;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;


/**
 * @author mcnulty
 */
public class TestModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(String[].class).annotatedWith(Names.named("OP_PACKAGES")).toInstance(
                new String[] {
                        "net.udidb.engine.ops.impls",
                        "net.udidb.engine.ops.parser.ut"
                });
    }
}
