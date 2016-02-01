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

import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.files.BinaryReader;
import net.sourcecrumbs.api.files.Executable;
import net.sourcecrumbs.api.files.UnknownFormatException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.ops.annotations.GlobalOperation;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.ValueResult;

/**
 * Operation to create a process
 *
 * @author mcnulty
 */
@HelpMessage("Create a new debuggee")
@DisplayName("create")
@GlobalOperation
public class CreateDebuggee extends DisplayNameOperation {

    private final DebuggeeContextManager contextManager;

    private final BinaryReader reader;

    @Operand(order=0)
    @HelpMessage("the full path to the executable")
    private String execPath;

    @Operand(order=1, restOfLine=true, optional=true)
    @HelpMessage("the arguments for the execution of the executable")
    private String[] args;

    @Inject
    public CreateDebuggee(DebuggeeContextManager contextManager, BinaryReader reader) {
        this.contextManager = contextManager;
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

        String[] localArgs;
        if (args == null) {
            localArgs = new String[0];
        }else{
            localArgs = args;
        }

        Executable executable;
        try {
            executable = reader.openExecutable(path);
        }catch (IOException | UnknownFormatException e) {
            throw new OperationException("Failed to open " + path, e);
        }

        try {
            DebuggeeContext context = contextManager.createContext(path, localArgs, executable);
            return new ValueResult("Debuggee created for " + execPath, context.getId());
        }catch (UdiException e) {
            throw new OperationException("Failed to create process", e);
        }
    }
}
