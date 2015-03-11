/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.expr;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.transunit.SourceLanguage;
import net.sourcecrumbs.api.transunit.TranslationUnit;
import net.udidb.expr.ExecutionContext;
import net.udidb.expr.Expression;
import net.udidb.expr.ExpressionCompiler;
import net.udidb.expr.ExpressionException;
import net.udidb.expr.lang.c.CExpressionCompiler;
import net.udidb.expr.stubs.StubExecutionContext;

/**
 * An ExpressionCompiler that delegates to a ExpressionCompiler configured for the current source language
 *
 * @author mcnulty
 */
public class ExpressionCompilerDelegate implements ExpressionCompiler
{
    private static final Logger logger = LoggerFactory.getLogger(ExpressionCompilerDelegate.class);

    // TODO implement operations to modify the configuration

    private final Map<SourceLanguage, ExpressionCompiler> compilers = new HashMap<>();

    private final SourceLanguage DEFAULT_LANGUAGE = SourceLanguage.C;

    public ExpressionCompilerDelegate()
    {
        compilers.put(SourceLanguage.C, new CExpressionCompiler());
    }

    @Override
    public Expression compile(String expression, ExecutionContext executionContext) throws ExpressionException
    {
        try {
            SourceLanguage currentLang = null;
            if (!executionContext.getCurrentThread().getParentProcess().isWaitingForStart()) {
                long currentPc = executionContext.getCurrentThread().getPC();

                TranslationUnit currentUnit = executionContext.getExecutable().getContainingTranslationUnit(currentPc);
                if (currentUnit != null) {
                    currentLang = currentUnit.getLanguage();
                }
            }

            if (currentLang == null) {
                currentLang = DEFAULT_LANGUAGE;
            }

            ExpressionCompiler compiler = compilers.get(currentLang);
            if (compiler == null) {
                throw new ExpressionException("No expression compiler configured for language " + currentLang);
            }

            return compiler.compile(expression, executionContext);
        }catch (UdiException e) {
            throw new ExpressionException(e);
        }
    }

    @Override
    public Expression compile(String expression) throws ExpressionException
    {
        return compile(expression, new StubExecutionContext());
    }
}
