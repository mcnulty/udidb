/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.parser.ut.TestModule;
import net.udidb.engine.ops.parser.ut.TestOp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mcnulty
 */
public class OperationParserTest
{
    private final Injector injector = Guice.createInjector(new TestModule());

    private void testArgs(String argsString, List<String> expectedArgs) throws Exception
    {
        OperationParser parser = injector.getInstance(OperationParser.class);
        Operation op = parser.parse("test " + argsString);
        assertTrue(op instanceof TestOp);
        assertEquals(expectedArgs, ((TestOp)op).getArgs());
    }

    @Test
    public void argsTest() throws Exception
    {
        testArgs("one two three", Arrays.asList("one", "two", "three"));
        testArgs("one \"two three\"", Arrays.asList("one", "two three"));
    }
}
