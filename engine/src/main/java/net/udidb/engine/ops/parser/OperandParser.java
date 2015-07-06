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

import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.annotations.Operand;

/**
 * Provides methods to parse an Operand
 *
 * @author mcnulty
 */
public interface OperandParser
{
    /**
     * @param token the token for the Operand
     * @param field the field for the Operand
     *
     * @return a value compatible with the type
     *
     * @throws OperationParseException when the token cannot be parsed into the appropriate type
     */
    Object parse(String token, Field field) throws OperationParseException;

    /**
     * @param restOfLineTokens the tokens for the rest of the line
     * @param field the field for the Operand
     *
     * @return a value compatible with the field
     *
     * @throws OperationParseException when the token cannot be parsed into the appropriate type
     */
    Object parse(List<String> restOfLineTokens, Field field) throws OperationParseException;
}
