/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import net.sourcecrumbs.api.debug.symbols.DebugType;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.expr.grammar.c.CParser;
import net.udidb.expr.grammar.c.CParser.ConstantContext;
import net.udidb.expr.grammar.c.CParser.FloatingConstantContext;
import net.udidb.expr.lang.c.values.AddressValue;
import net.udidb.expr.lang.c.values.NumberValue;
import net.udidb.expr.lang.c.values.StringValue;

/**
 * Visitor that simplifies and computes expressions that do not require debuggee execution
 *
 * @author dmcnulty
 */
public class ExpressionSimplificationVisitor extends BaseExpressionVisitor<Void>
{
    private static final String INVALID_TREE_MSG = "Unexpected parse tree state encountered";

    private final DebuggeeContext debuggeeContext;

    public ExpressionSimplificationVisitor(ParseTreeProperty<NodeState> states, DebuggeeContext debuggeeContext)
    {
        super(states);
        this.debuggeeContext = debuggeeContext;
    }

    @Override
    public Void visitIntegerConstant(@NotNull CParser.IntegerConstantContext ctx)
    {
        NodeState nodeState = getNodeState(ctx);

        String intConstantString;
        int base;
        if (ctx.DecimalConstant() != null) {
            base = 10;
            intConstantString = ctx.DecimalConstant().getText();
        }else if(ctx.HexadecimalConstant() != null) {
            base = 16;
            intConstantString = ctx.HexadecimalConstant().getText().substring(2);
        }else if(ctx.OctalConstant() != null) {
            base = 8;
            intConstantString = ctx.OctalConstant().getText().substring(1);
        }else{
            throw new ParseCancellationException(INVALID_TREE_MSG);
        }

        Number value;
        DebugType intType = nodeState.getEffectiveType();
        if (intType == Types.getType(Types.UNSIGNED_INT_NAME)) {
            value = Integer.parseUnsignedInt(intConstantString, base);
        }else if (intType == Types.getType(Types.SIGNED_INT_NAME)) {
            value = Integer.parseInt(intConstantString, base);
        }else if (intType == Types.getType(Types.UNSIGNED_LONG_NAME)) {
            value = Long.parseUnsignedLong(intConstantString, base);
        }else if (intType == Types.getType(Types.SIGNED_LONG_NAME)) {
            value = Long.parseLong(intConstantString, base);
        }else if (intType == Types.getType(Types.UNSIGNED_LONG_LONG_NAME)) {
            // TODO Need to figure out how to create a BigInteger from a String
            throw new ParseCancellationException("unsigned long long is currently not supported");
        }else if (intType == Types.getType(Types.SIGNED_LONG_LONG_NAME)) {
            throw new ParseCancellationException("long long is currently not supported");
        }else{
            throw new ParseCancellationException(INVALID_TREE_MSG);
        }

        nodeState.setExpressionValue(new NumberValue(value));
        return null;
    }

    @Override
    public Void visitFloatingConstant(@NotNull FloatingConstantContext ctx)
    {
        NodeState nodeState = getNodeState(ctx);

        Number value;
        DebugType floatType = nodeState.getEffectiveType();
        if (floatType == Types.getType(Types.FLOAT_NAME)) {
            value = Float.parseFloat(ctx.getText());
        }else if (floatType == Types.getType(Types.DOUBLE_NAME)) {
            value = Double.parseDouble(ctx.getText());
        }else{
            throw new ParseCancellationException(INVALID_TREE_MSG);
        }

        nodeState.setExpressionValue(new NumberValue(value));
        return null;
    }

    @Override
    public Void visitStringLiteral(@NotNull CParser.StringLiteralContext ctx)
    {
        getNodeState(ctx).setExpressionValue(new StringValue(ctx.getText()));
        return null;
    }

    @Override
    public Void visitAdditiveExpression(@NotNull CParser.AdditiveExpressionContext ctx)
    {
        NodeState nodeState = getNodeState(ctx);

        ParserRuleContext multExpr = ctx.multiplicativeExpression();
        multExpr.accept(this);

        if (ctx.additiveExpression() == null) {
            nodeState.setExpressionValue(getNodeState(multExpr).getExpressionValue());
            return null;
        }

        ParserRuleContext addExpr = ctx.additiveExpression();
        addExpr.accept(this);
        if (getNodeState(addExpr).getExpressionValue() == null || getNodeState(multExpr).getExpressionValue() == null) {
            // The computation cannot be performed yet
            return null;
        }

        String operation = ctx.getChild(1).getText();

        Number left = getNodeState(addExpr).getExpressionValue().getNumberValue();
        Number right = getNodeState(multExpr).getExpressionValue().getNumberValue();

        DebugType effectiveType = nodeState.getEffectiveType();
        Number computedValue;
        switch (operation) {
            case "+":
                if (effectiveType == Types.getType(Types.FLOAT_NAME)) {
                    computedValue = left.floatValue() + right.floatValue();
                }else if (effectiveType == Types.getType(Types.DOUBLE_NAME)) {
                    computedValue = left.doubleValue() + right.doubleValue();
                }else{
                    // TODO handle other cases
                    computedValue = 0;
                }
                break;
            case "-":
                // TODO implement
                computedValue = 0;
                break;
            default:
                throw new ParseCancellationException(INVALID_TREE_MSG);
        }

        nodeState.setExpressionValue(new NumberValue(computedValue));
        return null;
    }

    @Override
    public Void visitConstant(@NotNull ConstantContext ctx)
    {
        super.visitConstant(ctx);
        if (ctx.characterConstant() != null) {
            getNodeState(ctx).setExpressionValue(getNodeState(ctx.characterConstant()).getExpressionValue());
        }else if (ctx.floatingConstant() != null) {
            getNodeState(ctx).setExpressionValue(getNodeState(ctx.floatingConstant()).getExpressionValue());
        }else if (ctx.integerConstant() != null) {
            getNodeState(ctx).setExpressionValue(getNodeState(ctx.integerConstant()).getExpressionValue());
        }else{
            throw new ParseCancellationException(INVALID_TREE_MSG);
        }
        return null;
    }

    @Override
    public Void visitAndExpression(@NotNull CParser.AndExpressionContext ctx)
    {
        super.visitAndExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.equalityExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitAssignmentExpression(@NotNull CParser.AssignmentExpressionContext ctx)
    {
        super.visitAssignmentExpression(ctx);
        if (ctx.unaryExpression() != null) {
            // For now, this will require execution in the debuggee. There may be some optimizations that can be made where
            // a memory location/register can just be updated
            return null;
        }
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.conditionalExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitCastExpression(@NotNull CParser.CastExpressionContext ctx)
    {
        super.visitCastExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.unaryExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitConditionalExpression(@NotNull CParser.ConditionalExpressionContext ctx)
    {
        super.visitConditionalExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.logicalOrExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitConstantExpression(@NotNull CParser.ConstantExpressionContext ctx)
    {
        super.visitConstantExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.conditionalExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitExpression(@NotNull CParser.ExpressionContext ctx)
    {
        super.visitExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.assignmentExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitExclusiveOrExpression(@NotNull CParser.ExclusiveOrExpressionContext ctx)
    {
        super.visitExclusiveOrExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.andExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitEqualityExpression(@NotNull CParser.EqualityExpressionContext ctx)
    {
        super.visitEqualityExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.relationalExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitInclusiveOrExpression(@NotNull CParser.InclusiveOrExpressionContext ctx)
    {
        super.visitInclusiveOrExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.exclusiveOrExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitLogicalAndExpression(@NotNull CParser.LogicalAndExpressionContext ctx)
    {
        super.visitLogicalAndExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.inclusiveOrExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitLogicalOrExpression(@NotNull CParser.LogicalOrExpressionContext ctx)
    {
        super.visitLogicalOrExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.logicalAndExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitMultiplicativeExpression(@NotNull CParser.MultiplicativeExpressionContext ctx)
    {
        super.visitMultiplicativeExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.castExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitPostfixExpression(@NotNull CParser.PostfixExpressionContext ctx)
    {
        super.visitPostfixExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.primaryExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitPrimaryExpression(@NotNull CParser.PrimaryExpressionContext ctx)
    {
        super.visitPrimaryExpression(ctx);

        NodeState nodeState = getNodeState(ctx);
        if (nodeState.getFunction() != null) {
            nodeState.setExpressionValue(new AddressValue(nodeState.getFunction().getEntryAddress()));
        }else if (nodeState.getSymbol() != null) {
            nodeState.setExpressionValue(new AddressValue(nodeState.getSymbol().getAddress()));
        }else{
            // TODO need to handle variable -- the value of a variable may be available by interrogating the debuggee
            nodeState.setExpressionValue(getNodeState(ctx.constant()).getExpressionValue());
        }

        return null;
    }

    @Override
    public Void visitRelationalExpression(@NotNull CParser.RelationalExpressionContext ctx)
    {
        super.visitRelationalExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.shiftExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitShiftExpression(@NotNull CParser.ShiftExpressionContext ctx)
    {
        super.visitShiftExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.additiveExpression()).getExpressionValue());
        return null;
    }

    @Override
    public Void visitUnaryExpression(@NotNull CParser.UnaryExpressionContext ctx)
    {
        super.visitUnaryExpression(ctx);
        getNodeState(ctx).setExpressionValue(getNodeState(ctx.postfixExpression()).getExpressionValue());
        return null;
    }
}
