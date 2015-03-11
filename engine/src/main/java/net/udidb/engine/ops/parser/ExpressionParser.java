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

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.annotations.ExpressionConstraint;
import net.udidb.expr.Expression;
import net.udidb.expr.ExpressionCompiler;
import net.udidb.expr.ExpressionException;
import net.udidb.expr.values.ExpressionValue;

/**
 * An OperandParser that parses an operand or collection of operands as an expression
 *
 * @author mcnulty
 */
public class ExpressionParser implements OperandParser
{
    private final ExpressionCompiler expressionCompiler;
    private final DebuggeeContext debuggeeContext;

    @Inject
    public ExpressionParser(ExpressionCompiler expressionCompiler, @Nullable DebuggeeContext debuggeeContext)
    {
        this.expressionCompiler = expressionCompiler;
        this.debuggeeContext = debuggeeContext;
    }

    @Override
    public Expression parse(String token, Field field) throws OperationParseException
    {
        if (Expression.class.isAssignableFrom(field.getType())) {
            try {
                Expression expression;
                if (debuggeeContext == null) {
                    expression = expressionCompiler.compile(token);
                }else{
                    expression = expressionCompiler.compile(token, debuggeeContext);
                }

                ExpressionConstraint constraint = field.getAnnotation(ExpressionConstraint.class);
                if (constraint != null) {
                    if (!expression.isExpressionCompleted() && !constraint.executionAllowed()) {
                        throw new OperationParseException("This operation does not support expressions requiring execution in the debuggee");
                    }

                    ExpressionValue value = expression.getValue();
                    if (value != null && value.getType() != constraint.expectedType()) {
                        throw new OperationParseException("The resulting expression is of type '" + value.getType() + "', expected '" +
                                constraint.expectedType() + "'");
                    }
                }

                return expression;
            } catch (ExpressionException e) {
                throw new OperationParseException(e);
            }
        }

        throw new OperationParseException("Target type of " + field.getType().getSimpleName() + " is not compatible with Expression");
    }

    @Override
    public Object parse(List<String> restOfLineTokens, Field field) throws OperationParseException
    {
        return parse(StringUtils.join(restOfLineTokens, " "), field);
    }
}
