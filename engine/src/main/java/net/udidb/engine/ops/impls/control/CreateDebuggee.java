/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.control;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;

import net.libudi.api.UdiProcessManager;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.files.BinaryReader;
import net.sourcecrumbs.api.files.Executable;
import net.sourcecrumbs.api.files.UnknownFormatException;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.VoidResult;

/**
 * Operation to create a process
 *
 * @author mcnulty
 */
@HelpMessage(enMessage="Create a new debuggee")
@LongHelpMessage(enMessage=
        "create /path/to/executable args\n\n" +
        "Create a new debuggee"
)
@DisplayName("create")
public class CreateDebuggee extends DisplayNameOperation {

    private final DebuggeeContextManager contextFactory;

    private final BinaryReader reader;

    @Operand(order=0)
    private String execPath;

    @Operand(order=1, restOfLine=true, optional=true)
    private String[] args;

    @Inject
    public CreateDebuggee(DebuggeeContextManager contextFactory, BinaryReader reader) {
        this.contextFactory = contextFactory;
        this.reader = reader;
    }

    public String getExecPath() {
        return execPath;
    }

    public void setExecPath(String execPath) {
        this.execPath = execPath;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public Result execute() throws OperationException {
        Path path;
        try {
            path = Paths.get(execPath);
        }catch (InvalidPathException e) {
            throw new OperationException(String.format("%s is not a valid path", execPath), e);
        }

        Executable executable;
        try {
            executable = reader.openExecutable(path);
        }catch (IOException | UnknownFormatException e) {
            throw new OperationException("Failed to open " + path, e);
        }

        try {
            contextFactory.createContext(path, args, executable);
        }catch (UdiException e) {
            throw new OperationException("Failed to create process", e);
        }


        return new VoidResult();
    }
}
