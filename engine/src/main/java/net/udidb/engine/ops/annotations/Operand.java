/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.udidb.engine.ops.parser.OperandParser;
import net.udidb.engine.ops.parser.PassThroughOperationParser;

/**
 * An annotation that marks an operand field in an Operation
 *
 * @author mcnulty
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Operand {

    /**
     * @return the order of the operand to the Operation when being specified in a String (0-based index)
     */
    int order();

    /**
     * @return true, if the operand is optional. Only the last operands (in order) can be optional.
     */
    boolean optional() default false;

    /**
     * @return true, if the operand consumes the rest of the line
     */
    boolean restOfLine() default false;

    /**
     * @return the class used to parse the Operand
     */
    Class<? extends OperandParser> operandParser() default PassThroughOperationParser.class;
}
