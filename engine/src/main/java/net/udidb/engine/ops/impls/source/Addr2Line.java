/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.source;


import com.google.inject.Inject;

import net.sourcecrumbs.api.machinecode.MachineCodeMapping;
import net.sourcecrumbs.api.transunit.NoSuchLineException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.MissingDebugInfoException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.impls.ContextExpressionOperation;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.source.SourceLineRow;
import net.udidb.engine.source.SourceLineRowFactory;
import net.udidb.expr.Expression;

/**
 * An operation to obtain the source line for the specified address
 *
 * @author mcnulty
 */
@HelpMessage("Obtain the source line for the specified address")
@DisplayName("source addr2line")
public class Addr2Line extends ContextExpressionOperation
{
    private final SourceLineRowFactory sourceLineRowFactory;

    @Inject
    Addr2Line(SourceLineRowFactory sourceLineRowFactory) {
        this.sourceLineRowFactory = sourceLineRowFactory;
    }

    @Override
    protected Result executeWithExpression(DebuggeeContext context, Expression expression) throws OperationException
    {
        long addressValue;
        switch (expression.getValue().getType()) {
            case ADDRESS:
                addressValue = expression.getValue().getAddressValue();
                break;
            case NUMBER:
                addressValue = expression.getValue().getNumberValue().longValue();
                break;
            default:
                throw new OperationException("Expression value cannot be used to obtain the source line");
        }

        MachineCodeMapping machineCodeMapping = context.getExecutable().getMachineCodeMapping();
        if (machineCodeMapping == null) {
            throw new MissingDebugInfoException();
        }

        try {
            return new TableResult(sourceLineRowFactory.create(machineCodeMapping.getSourceLinesRanges(addressValue)));
        }catch (NoSuchLineException e) {
            return new TableResult(new SourceLineRow());
        }
    }
}
