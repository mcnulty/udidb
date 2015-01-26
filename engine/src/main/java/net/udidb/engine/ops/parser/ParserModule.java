/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * A Guice module defining dependencies for the Operation parser
 *
 * @author mcnulty
 */
public class ParserModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("OP_IMPL_PACKAGE")).toInstance("net.udidb.engine.ops.impls");
    }
}
