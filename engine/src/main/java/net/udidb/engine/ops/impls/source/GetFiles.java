/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.source;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextAware;
import net.udidb.engine.ops.NoDebuggeeContextException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.TableRow;

/**
 * Obtains all the source files for the current debuggee context
 *
 * @author mcnulty
 */
@HelpMessage("Obtain files from the current debuggee context")
@DisplayName("source files")
public class GetFiles extends DisplayNameOperation implements DebuggeeContextAware
{
    private DebuggeeContext debuggeeContext;

    @Override
    public void setDebuggeeContext(DebuggeeContext debuggeeContext)
    {
        this.debuggeeContext = debuggeeContext;
    }

    @Override
    public Result execute() throws OperationException
    {
        if (debuggeeContext == null) {
            throw new NoDebuggeeContextException();
        }

        return new TableResult(StreamSupport.stream(debuggeeContext.getExecutable().getTranslationUnits().spliterator(), false)
                                            .filter(t -> t.getPath() != null)
                                            .map(t -> new FileRow(t.getPath()))
                                            .collect(Collectors.toList()));
    }

    private static class FileRow implements TableRow {

        private final Path file;

        public FileRow(Path file) {
            this.file = file;
        }

        @Override
        public List<String> getColumnHeaders()
        {
            return Collections.singletonList("File");
        }

        @Override
        public List<String> getColumnValues()
        {
            return Collections.singletonList(file.toAbsolutePath().toString());
        }
    }
}
