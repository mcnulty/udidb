/*
 * Copyright (c) 2011-2013, Dan McNulty
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the UDI project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package net.udidb.engine.ops.impls.control;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.inject.Inject;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessManager;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.ops.DisplayNameOperation;
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

    private final UdiProcessManager procManager;

    private final DebuggeeContextFactory contextFactory;

    @Operand(order=0)
    private String execPath;

    @Operand(order=1, restOfLine=true)
    private String[] args;

    @Inject
    public CreateDebuggee(UdiProcessManager procManager, DebuggeeContextFactory contextFactory) {
        this.procManager = procManager;
        this.contextFactory = contextFactory;
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

        DebuggeeContext context = contextFactory.createContext(path, args);

        UdiProcess process;
        try {
            process = procManager.createProcess(path, args, context.getEnv(), context.createProcessConfig());
        }catch (UdiException e) {
            throw new OperationException(e.getMessage(), e);
        }

        context.setProcess(process);

        return new VoidResult();
    }
}
