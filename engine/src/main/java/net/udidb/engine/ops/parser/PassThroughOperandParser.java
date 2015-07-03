/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser;

import java.lang.reflect.Field;
import java.util.List;

import com.google.inject.Singleton;

import net.udidb.engine.ops.OperationParseException;

/**
 * The default implementation of OperandParser
 *
 * @author mcnulty
 */
@Singleton
public class PassThroughOperandParser implements OperandParser
{

    @Override
    public Object parse(String token, Field field) throws OperationParseException
    {
        return token;
    }

    @Override
    public Object parse(List<String> restOfLineTokens, Field field) throws OperationParseException
    {
        return restOfLineTokens;
    }
}
