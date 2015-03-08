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

import net.udidb.expr.values.ValueType;

/**
 * An annotation that allows an Operation to specify some constraints on an Expression tied to an Operand.
 *
 * @author mcnulty
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpressionConstraint
{
    /**
     * @return the expected type of the ExpressionValue associated with the expression. If the Expression requires execution,
     *         this value will be unused.
     */
    ValueType expectedType();

    /**
     * @return true if the consumer of the expression supports execution in the debuggee to compute the expression
     */
    boolean executionAllowed() default true;
}
