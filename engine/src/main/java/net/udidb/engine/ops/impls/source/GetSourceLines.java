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

import net.sourcecrumbs.api.Range;
import net.sourcecrumbs.api.machinecode.MachineCodeMapping;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextAware;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.parser.RangeParser;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.source.SourceLineRowFactory;

/**
 * An Operation to obtain lines of source from a specified file
 *
 * @author mcnulty
 */
@HelpMessage("Obtain lines of source from a file")
@DisplayName("source lines")
public class GetSourceLines extends DisplayNameOperation implements DebuggeeContextAware
{
    private final SourceLineRowFactory sourceLineRowFactory;
    private DebuggeeContext debuggeeContext;

    @HelpMessage("the range of source lines in the file")
    @Operand(order = 0, operandParser = RangeParser.class)
    private Range<Integer> range;

    @HelpMessage("the name of the source file as defined in the binary. " +
                 "If unspecified, the current translation unit is used, if available.")
    @Operand(order = 10, optional = true)
    private String file;

    @Inject
    GetSourceLines(SourceLineRowFactory sourceLineRowFactory) {
        this.sourceLineRowFactory = sourceLineRowFactory;
    }

    public Range<Integer> getRange()
    {
        return range;
    }

    public void setRange(Range<Integer> range)
    {
        this.range = range;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    @Override
    public Result execute() throws OperationException
    {
        if (file != null) {
            MachineCodeMapping codeMapping = debuggeeContext.getExecutable()
        }
        return null;
    }

    @Override
    public void setDebuggeeContext(DebuggeeContext debuggeeContext)
    {
        this.debuggeeContext = debuggeeContext;
    }
}
