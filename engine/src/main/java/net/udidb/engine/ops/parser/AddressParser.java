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
 * Converts an address string (in various formats) to a Long
 *
 * @author mcnulty
 */
@Singleton
public class AddressParser implements OperandParser
{
    @Override
    public Object parse(String token, Field field) throws OperationParseException
    {
        if (long.class.isAssignableFrom(field.getType())) {
            // Try hex first
            try {
                String valueString;
                if (token.toString().startsWith("0x")) {
                    valueString = token.toString().replaceFirst("0x", "");
                }else{
                    valueString = token.toString();
                }
                return Long.parseLong(valueString, 16);
            }catch (NumberFormatException e) {
            }

            // Try decimal second
            try {
                return Long.parseLong(token.toString(), 10);
            }catch (NumberFormatException e) {
            }

            // Try octal
            try {
                return Long.parseLong(token.toString(), 8);
            }catch (NumberFormatException e) {
            }
        }

        throw new OperationParseException(String.format("Failed to convert value to convert %s to long", token));
    }

    @Override
    public Object parse(List<String> restOfLineTokens, Field field) throws OperationParseException
    {
        throw new OperationParseException("Cannot convert collection of tokens into an address");
    }
}
