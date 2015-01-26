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
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.expr.grammar.c.CParser;

/**
 * Visitor that simplifies and computes expressions that do not require debuggee execution
 *
 * @author dmcnulty
 */
public class ExpressionSimplificationVisitor extends BaseExpressionVisitor<ParserRuleContext>
{
    private final DebuggeeContext debuggeeContext;

    public ExpressionSimplificationVisitor(ParseTreeProperty<NodeState> states, DebuggeeContext debuggeeContext)
    {
        super(states);
        this.debuggeeContext = debuggeeContext;
    }

    @Override
    public ParserRuleContext visitAdditiveExpression(@NotNull CParser.AdditiveExpressionContext ctx)
    {
        return super.visitAdditiveExpression(ctx);
    }

    @Override
    public ParserRuleContext visitAndExpression(@NotNull CParser.AndExpressionContext ctx)
    {
        return super.visitAndExpression(ctx);
    }

    @Override
    public ParserRuleContext visitAssignmentExpression(@NotNull CParser.AssignmentExpressionContext ctx)
    {
        return super.visitAssignmentExpression(ctx);
    }

    @Override
    public ParserRuleContext visitCastExpression(@NotNull CParser.CastExpressionContext ctx)
    {
        return super.visitCastExpression(ctx);
    }

    @Override
    public ParserRuleContext visitConditionalExpression(@NotNull CParser.ConditionalExpressionContext ctx)
    {
        return super.visitConditionalExpression(ctx);
    }

    @Override
    public ParserRuleContext visitConstant(@NotNull CParser.ConstantContext ctx)
    {
        return super.visitConstant(ctx);
    }

    @Override
    public ParserRuleContext visitConstantExpression(@NotNull CParser.ConstantExpressionContext ctx)
    {
        return super.visitConstantExpression(ctx);
    }

    @Override
    public ParserRuleContext visitDecimalFloatingConstant(@NotNull CParser.DecimalFloatingConstantContext ctx)
    {
        return super.visitDecimalFloatingConstant(ctx);
    }

    @Override
    public ParserRuleContext visitExpression(@NotNull CParser.ExpressionContext ctx)
    {
        return super.visitExpression(ctx);
    }

    @Override
    public ParserRuleContext visitExclusiveOrExpression(@NotNull CParser.ExclusiveOrExpressionContext ctx)
    {
        return super.visitExclusiveOrExpression(ctx);
    }

    @Override
    public ParserRuleContext visitEqualityExpression(@NotNull CParser.EqualityExpressionContext ctx)
    {
        return super.visitEqualityExpression(ctx);
    }

    @Override
    public ParserRuleContext visitFloatingConstant(@NotNull CParser.FloatingConstantContext ctx)
    {
        return super.visitFloatingConstant(ctx);
    }

    @Override
    public ParserRuleContext visitHexadecimalFloatingConstant(@NotNull CParser.HexadecimalFloatingConstantContext ctx)
    {
        return super.visitHexadecimalFloatingConstant(ctx);
    }

    @Override
    public ParserRuleContext visitInclusiveOrExpression(@NotNull CParser.InclusiveOrExpressionContext ctx)
    {
        return super.visitInclusiveOrExpression(ctx);
    }

    @Override
    public ParserRuleContext visitIntegerConstant(@NotNull CParser.IntegerConstantContext ctx)
    {
        return super.visitIntegerConstant(ctx);
    }

    @Override
    public ParserRuleContext visitLogicalAndExpression(@NotNull CParser.LogicalAndExpressionContext ctx)
    {
        return super.visitLogicalAndExpression(ctx);
    }

    @Override
    public ParserRuleContext visitLogicalOrExpression(@NotNull CParser.LogicalOrExpressionContext ctx)
    {
        return super.visitLogicalOrExpression(ctx);
    }

    @Override
    public ParserRuleContext visitMultiplicativeExpression(@NotNull CParser.MultiplicativeExpressionContext ctx)
    {
        return super.visitMultiplicativeExpression(ctx);
    }

    @Override
    public ParserRuleContext visitPostfixExpression(@NotNull CParser.PostfixExpressionContext ctx)
    {
        return super.visitPostfixExpression(ctx);
    }

    @Override
    public ParserRuleContext visitPrimaryExpression(@NotNull CParser.PrimaryExpressionContext ctx)
    {
        return super.visitPrimaryExpression(ctx);
    }

    @Override
    public ParserRuleContext visitRelationalExpression(@NotNull CParser.RelationalExpressionContext ctx)
    {
        return super.visitRelationalExpression(ctx);
    }

    @Override
    public ParserRuleContext visitShiftExpression(@NotNull CParser.ShiftExpressionContext ctx)
    {
        return super.visitShiftExpression(ctx);
    }

    @Override
    public ParserRuleContext visitUnaryExpression(@NotNull CParser.UnaryExpressionContext ctx)
    {
        return super.visitUnaryExpression(ctx);
    }

    @Override
    public ParserRuleContext visitStringLiteral(@NotNull CParser.StringLiteralContext ctx)
    {
        return super.visitStringLiteral(ctx);
    }
}
