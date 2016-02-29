/*
 * Copyright (c) 2011-2016, Dan McNulty
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

import net.sourcecrumbs.api.Range;
import net.udidb.engine.ops.OperationParseException;

/**
 * @author mcnulty
 */
@Singleton
public class RangeParser implements OperandParser
{

    @Override
    public Object parse(String token, Field field) throws OperationParseException
    {
        if (Range.class.isAssignableFrom(field.getType())) {
            String[] elements = token.split(":");
            if (elements.length != 2) {
                throw new OperationParseException("Invalid range format " + token);
            }

            int start;
            try {
                start = Integer.parseInt(elements[0]);
            }catch (NumberFormatException e) {
                throw new OperationParseException("Invalid start element in range " + token, e);
            }

            int end;
            try {
                end = Integer.parseInt(elements[1]);
            }catch (NumberFormatException e) {
                throw new OperationParseException("Invalid end element in range " + token, e);
            }

            return new Range<>(start, end);
        }

        throw new OperationParseException("Target type " + field.getType().getSimpleName() + " is not compatible with Range");
    }

    @Override
    public Object parse(List<String> restOfLineTokens, Field field) throws OperationParseException
    {
        throw new OperationParseException("Rest of line property is invalid for Range operands");
    }
}
