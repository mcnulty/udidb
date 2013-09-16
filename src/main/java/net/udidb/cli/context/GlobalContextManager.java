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

package net.udidb.cli.context;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import net.libudi.api.UdiProcess;
import net.udidb.engine.ops.context.DebuggeeContext;
import net.udidb.engine.ops.context.DebuggeeContextFactory;

/**
 * A class that maintains the current global DebuggeeContext that is passed to control Operations
 *
 * @author mcnulty
 */
@Singleton
public class GlobalContextManager implements Provider<DebuggeeContext>, DebuggeeContextFactory {

    private Map<String, String> env = null;

    private Path rootDir = Files.createTempDir().toPath();

    private DebuggeeContext current = null;

    private Map<Integer, DebuggeeContext> contexts = Collections.synchronizedMap(new HashMap<Integer, DebuggeeContext>());

    private final AtomicInteger currentId = new AtomicInteger(0);

    @Inject
    GlobalContextManager() {
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    public DebuggeeContext getCurrent() {
        return current;
    }

    public void setCurrent(DebuggeeContext current) {
        this.current = current;
    }

    public void setCurrent(int id) {
        DebuggeeContext currentContext = contexts.get(id);
        if (currentContext == null) {
            throw new IllegalArgumentException(String.format("No context with id '%s' exists.", id));
        }

        this.current = currentContext;
    }

    @Override
    public DebuggeeContext get() {
        return current;
    }

    @Override
    public DebuggeeContext createContext(Path execPath, String[] args) {
        DebuggeeContext context = new DebuggeeContext();

        context.setEnv(env == null ? null : new HashMap<>(env));
        context.setRootDir(rootDir);
        context.setExecPath(execPath);
        context.setArgs(args);

        contexts.put(currentId.getAndIncrement(), context);
        current = context;

        return context;
    }

    public List<UdiProcess> getProcesses() {
        List<UdiProcess> processes = new ArrayList<>();
        synchronized (contexts) {
            for (DebuggeeContext context : contexts.values()) {
                processes.add(context.getProcess());
            }
        }
        return processes;
    }
}
