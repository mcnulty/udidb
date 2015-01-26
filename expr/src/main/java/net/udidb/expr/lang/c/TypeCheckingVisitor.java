/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import net.sourcecrumbs.api.debug.symbols.DebugType;
import net.udidb.expr.grammar.c.CParser;

/**
 * Visitor that type checks the expression
 *
 * @author dmcnulty
 */
public class TypeCheckingVisitor extends BaseExpressionVisitor<Void>
{
    public TypeCheckingVisitor(ParseTreeProperty<NodeState> states)
    {
        super(states);
    }

    @Override
    public Void visitIntegerConstant(@NotNull CParser.IntegerConstantContext ctx)
    {
        NodeState nodeState = getNodeState(ctx);

        TerminalNode integerSuffix = ctx.IntegerSuffix();
        if (integerSuffix != null) {
            nodeState.setEffectiveType(Types.getIntegerSuffixType(integerSuffix.getText()));
        }else{
            // TODO investigate whether type should be coerced into short here if necessary
            nodeState.setEffectiveType(Types.getType(Types.SIGNED_INT_NAME));
        }

        return super.visitIntegerConstant(ctx);
    }

    private static void setFloatType(NodeState nodeState, TerminalNode floatSuffix)
    {
        if (floatSuffix != null) {
            nodeState.setEffectiveType(Types.getFloatSuffixType(floatSuffix.getText()));
        }else{
            nodeState.setEffectiveType(Types.getType(Types.DOUBLE_NAME));
        }

    }

    @Override
    public Void visitHexadecimalFloatingConstant(@NotNull CParser.HexadecimalFloatingConstantContext ctx)
    {
        setFloatType(getNodeState(ctx), ctx.FloatingSuffix());

        return super.visitHexadecimalFloatingConstant(ctx);
    }

    @Override
    public Void visitDecimalFloatingConstant(@NotNull CParser.DecimalFloatingConstantContext ctx)
    {
        setFloatType(getNodeState(ctx), ctx.FloatingSuffix());

        return super.visitDecimalFloatingConstant(ctx);
    }

    @Override
    public Void visitFloatingConstant(@NotNull CParser.FloatingConstantContext ctx)
    {
        Void retValue = super.visitFloatingConstant(ctx);

        CParser.DecimalFloatingConstantContext decimal = ctx.decimalFloatingConstant();
        CParser.HexadecimalFloatingConstantContext hex = ctx.hexadecimalFloatingConstant();

        if (decimal != null) {
            getNodeState(ctx).setEffectiveType(getNodeState(decimal).getEffectiveType());
        }else if (hex != null) {
            getNodeState(ctx).setEffectiveType(getNodeState(hex).getEffectiveType());
        }

        return retValue;
    }

    @Override
    public Void visitCharacterConstant(@NotNull CParser.CharacterConstantContext ctx)
    {
        NodeState nodeState = getNodeState(ctx);

        TerminalNode stdCharConstant = ctx.StandardCharacterConstant();
        TerminalNode utf16CharConstant = ctx.Utf16CharacterConstant();
        TerminalNode utf32CharConstant = ctx.Utf32CharacterConstant();
        TerminalNode wideCharConstant = ctx.WideCharacterConstant();

        if (stdCharConstant != null) {
            nodeState.setEffectiveType(Types.getType(Types.CHAR_NAME));
        }
        if (utf16CharConstant != null) {
            nodeState.setEffectiveType(Types.getType(Types.CHAR16_NAME));
        }
        if (utf32CharConstant != null) {
            nodeState.setEffectiveType(Types.getType(Types.CHAR32_NAME));
        }
        if (wideCharConstant != null) {
            nodeState.setEffectiveType(Types.getType(Types.WIDE_CHAR_NAME));
        }

        return super.visitCharacterConstant(ctx);
    }

    @Override
    public Void visitConstant(@NotNull CParser.ConstantContext ctx)
    {
        Void retValue = super.visitConstant(ctx);

        CParser.IntegerConstantContext integerConstant = ctx.integerConstant();
        CParser.FloatingConstantContext floatingConstant = ctx.floatingConstant();
        CParser.CharacterConstantContext characterConstantContext = ctx.characterConstant();

        if (integerConstant != null) {
            getNodeState(ctx).setEffectiveType(getNodeState(integerConstant).getEffectiveType());
        }else if (floatingConstant != null) {
            getNodeState(ctx).setEffectiveType(getNodeState(floatingConstant).getEffectiveType());
        }else if (characterConstantContext != null) {
            getNodeState(ctx).setEffectiveType(getNodeState(characterConstantContext).getEffectiveType());
        }

        return retValue;
    }

    @Override
    public Void visitStringLiteral(@NotNull CParser.StringLiteralContext ctx)
    {
        NodeState nodeState = getNodeState(ctx);

        TerminalNode utf8String = ctx.Utf8String();
        TerminalNode utf16String = ctx.Utf16String();
        TerminalNode utf32String = ctx.Utf32String();
        TerminalNode wideString = ctx.WideString();

        if (utf8String != null) {
            nodeState.setEffectiveType(Types.getType(Types.CHAR_NAME, true));
        }
        if (utf16String != null) {
            nodeState.setEffectiveType(Types.getType(Types.CHAR16_NAME, true));
        }
        if (utf32String != null) {
            nodeState.setEffectiveType(Types.getType(Types.CHAR32_NAME, true));
        }
        if (wideString != null) {
            nodeState.setEffectiveType(Types.getType(Types.WIDE_CHAR_NAME, true));
        }

        return super.visitStringLiteral(ctx);
    }

    @Override
    public Void visitPrimaryExpression(@NotNull CParser.PrimaryExpressionContext ctx)
    {
        Void retValue = super.visitPrimaryExpression(ctx);

        NodeState nodeState = getNodeState(ctx);

        TerminalNode identifier = ctx.Identifier();
        CParser.ConstantContext constant = ctx.constant();
        if (identifier != null) {
            DebugType effectiveType;
            if (nodeState.getFunction() != null) {
                effectiveType = nodeState.getFunction().getReturnType();
            }else if (nodeState.getSymbol() != null) {
                // Symbols are assumed to be void *
                effectiveType = Types.getType(Types.VOID_NAME, true);
            }else if (nodeState.getVariable() != null) {
                effectiveType = nodeState.getVariable().getType();
            }else{
                throw new ParseCancellationException("Could not determine type for '" + identifier + "'");
            }

            // Remove typedefs at this point
            if (effectiveType.getBaseType() != null) {
                effectiveType = effectiveType.getBaseType();
            }
            nodeState.setEffectiveType(effectiveType);
        }else if (constant != null) {
            nodeState.setEffectiveType(getNodeState(constant).getEffectiveType());
        }

        return retValue;
    }

    private static DebugType calculateArithmeticOperationType(DebugType left, DebugType right)
    {
        String incompatibleTypesMessage = String.format("Incompatible types used in arithmetic expression: " +
                                                           "left = '%s' and right = '%s'",
                                                           left.getName(),
                                                           right.getName());

        if (Types.equals(left, right)) {
            return left;
        }

        // Pointer arithmetic
        if (left.isPointer()) {
            if (Types.isIntegerType(right)) {
                return left;
            }
            throw new ParseCancellationException(incompatibleTypesMessage);
        }
        if (right.isPointer()) {
            if (Types.isIntegerType(left)) {
                return right;
            }
            throw new ParseCancellationException(incompatibleTypesMessage);
        }

        // Float types
        if (Types.isFloatType(left) && Types.isFloatType(right)) {
            if (left.getName().equals(Types.LONG_DOUBLE_NAME)) {
                return left;
            }
            if (right.getName().equals(Types.LONG_DOUBLE_NAME)) {
                return right;
            }
            if (left.getName().equals(Types.DOUBLE_NAME)) {
                return left;
            }
            if (right.getName().equals(Types.DOUBLE_NAME)) {
                return right;
            }
            return left;
        }

        if (Types.isFloatType(left) && Types.isIntegerType(right)) {
            return left;
        }

        if (Types.isIntegerType(left) && Types.isFloatType(right)) {
            return right;
        }

        // Integer types
        if (Types.isSignedIntegerType(left) && Types.isSignedIntegerType(right)) {
            if (left.getName().equals(Types.SIGNED_LONG_LONG_NAME)) {
                return left;
            }
            if (right.getName().equals(Types.SIGNED_LONG_LONG_NAME)) {
                return right;
            }
            if (left.getName().equals(Types.SIGNED_LONG_NAME)) {
                return left;
            }
            if (right.getName().equals(Types.SIGNED_LONG_NAME)) {
                return right;
            }
            return left;
        }
        if (Types.isUnsignedIntegerType(left) && Types.isUnsignedIntegerType(right)) {
            if (left.getName().equals(Types.UNSIGNED_LONG_LONG_NAME)) {
                return left;
            }
            if (right.getName().equals(Types.UNSIGNED_LONG_LONG_NAME)) {
                return right;
            }
            if (left.getName().equals(Types.UNSIGNED_LONG_NAME)) {
                return left;
            }
            if (right.getName().equals(Types.UNSIGNED_LONG_NAME)) {
                return right;
            }
            return left;
        }

        if (Types.isSignedIntegerType(left) && Types.isUnsignedIntegerType(right)) {
            return left;
        }
        if (Types.isUnsignedIntegerType(left) && Types.isSignedIntegerType(right)) {
            return right;
        }

        throw new ParseCancellationException(incompatibleTypesMessage);
    }

    @Override
    public Void visitPostfixExpression(@NotNull CParser.PostfixExpressionContext ctx)
    {
        Void retValue = super.visitPostfixExpression(ctx);

        // TODO handle postfix operations

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.primaryExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitCastExpression(@NotNull CParser.CastExpressionContext ctx)
    {
        Void retValue = super.visitCastExpression(ctx);

        // TODO handle cast operations

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.unaryExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitUnaryExpression(@NotNull CParser.UnaryExpressionContext ctx)
    {
        Void retValue = super.visitUnaryExpression(ctx);

        // TODO handle unary operations

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.postfixExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitMultiplicativeExpression(@NotNull CParser.MultiplicativeExpressionContext ctx)
    {
        Void retValue = super.visitMultiplicativeExpression(ctx);

        // TODO handle multiplication
        getNodeState(ctx).setEffectiveType(getNodeState(ctx.castExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitAdditiveExpression(@NotNull CParser.AdditiveExpressionContext ctx)
    {
        Void retValue = super.visitAdditiveExpression(ctx);

        CParser.AdditiveExpressionContext left = ctx.additiveExpression();
        CParser.MultiplicativeExpressionContext right =  ctx.multiplicativeExpression();

        DebugType effectiveType;
        if (left != null) {
            effectiveType = calculateArithmeticOperationType(getNodeState(left).getEffectiveType(),
                                                             getNodeState(right).getEffectiveType());
        }else{
            effectiveType = getNodeState(right).getEffectiveType();
        }
        getNodeState(ctx).setEffectiveType(effectiveType);

        return retValue;
    }

    @Override
    public Void visitShiftExpression(@NotNull CParser.ShiftExpressionContext ctx)
    {
        Void retValue = super.visitShiftExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.additiveExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitRelationalExpression(@NotNull CParser.RelationalExpressionContext ctx)
    {
        Void retValue = super.visitRelationalExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.shiftExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitEqualityExpression(@NotNull CParser.EqualityExpressionContext ctx)
    {
        Void retValue = super.visitEqualityExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.relationalExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitAndExpression(@NotNull CParser.AndExpressionContext ctx)
    {
        Void retValue = super.visitAndExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.equalityExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitExclusiveOrExpression(@NotNull CParser.ExclusiveOrExpressionContext ctx)
    {
        Void retValue = super.visitExclusiveOrExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.andExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitInclusiveOrExpression(@NotNull CParser.InclusiveOrExpressionContext ctx)
    {
        Void retValue = super.visitInclusiveOrExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.exclusiveOrExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitLogicalAndExpression(@NotNull CParser.LogicalAndExpressionContext ctx)
    {
        Void retValue = super.visitLogicalAndExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.inclusiveOrExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitLogicalOrExpression(@NotNull CParser.LogicalOrExpressionContext ctx)
    {
        Void retValue = super.visitLogicalOrExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.logicalAndExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitConditionalExpression(@NotNull CParser.ConditionalExpressionContext ctx)
    {
        Void retValue = super.visitConditionalExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.logicalOrExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitAssignmentExpression(@NotNull CParser.AssignmentExpressionContext ctx)
    {
        Void retValue = super.visitAssignmentExpression(ctx);

        getNodeState(ctx).setEffectiveType(getNodeState(ctx.conditionalExpression()).getEffectiveType());

        return retValue;
    }

    @Override
    public Void visitExpression(@NotNull CParser.ExpressionContext ctx)
    {
        try {
            Void retValue = super.visitExpression(ctx);

            getNodeState(ctx).setEffectiveType(getNodeState(ctx.assignmentExpression()).getEffectiveType());

            return retValue;
        }catch (NullPointerException e) {
            // This is a temporary measure to capture unimplemented cases and turn them into parse errors
            throw new ParseCancellationException(e);
        }
    }
}
