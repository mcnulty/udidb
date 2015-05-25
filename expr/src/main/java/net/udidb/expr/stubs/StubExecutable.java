/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.stubs;

import java.util.Collections;
import java.util.List;

import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.debug.symbols.Variable;
import net.sourcecrumbs.api.files.Executable;
import net.sourcecrumbs.api.files.Library;
import net.sourcecrumbs.api.machinecode.MachineCodeMapping;
import net.sourcecrumbs.api.symbols.Symbol;
import net.sourcecrumbs.api.transunit.TranslationUnit;

/**
 * @author mcnulty
 */
public class StubExecutable extends Executable
{
    @Override
    public List<Library> getLibraries()
    {
        return Collections.<Library>emptyList();
    }

    @Override
    public Iterable<Variable> getGlobalVariables()
    {
        return Collections.<Variable>emptyList();
    }

    @Override
    public Variable getGlobalVariable(String name)
    {
        return null;
    }

    @Override
    public Iterable<Function> getFunctions()
    {
        return Collections.<Function>emptyList();
    }

    @Override
    public Function getFunction(String name)
    {
        return null;
    }

    @Override
    public Function getContainingFunction(long pc)
    {
        return null;
    }

    @Override
    public MachineCodeMapping getMachineCodeMapping()
    {
        return null;
    }

    @Override
    public Iterable<Symbol> getSymbols()
    {
        return Collections.<Symbol>emptyList();
    }

    @Override
    public List<Symbol> getSymbolsByName(String name)
    {
        return null;
    }

    @Override
    public Iterable<TranslationUnit> getTranslationUnits()
    {
        return Collections.<TranslationUnit>emptyList();
    }

    @Override
    public TranslationUnit getContainingTranslationUnit(long pc)
    {
        return null;
    }
}
