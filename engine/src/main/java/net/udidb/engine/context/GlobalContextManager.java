/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.context;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
import net.libudi.api.UdiProcessManager;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.files.Executable;

/**
 * A class that maintains the current global DebuggeeContext that is passed to control Operations
 *
 * @author mcnulty
 */
@Singleton
public class GlobalContextManager implements Provider<DebuggeeContext>, DebuggeeContextFactory {

    // This must be null as a value of null has the environment inherited from the
    // creating process
    private Map<String, String> env = null;

    private Path rootDir = Files.createTempDir().toPath();

    private DebuggeeContext current = null;

    private Map<Integer, DebuggeeContext> contexts = Collections.synchronizedMap(new HashMap<Integer, DebuggeeContext>());

    private final AtomicInteger currentId = new AtomicInteger(0);

    private final UdiProcessManager udiProcessManager;

    @Inject
    GlobalContextManager(UdiProcessManager udiProcessManager)
    {
        this.udiProcessManager = udiProcessManager;
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

    public synchronized void setCurrent(DebuggeeContext current) {
        if (this.current != null) {
            this.current.setActive(false);
        }
        this.current = current;
        if (this.current != null) {
            this.current.setActive(true);
        }
    }

    public synchronized void setCurrent(int id) {
        DebuggeeContext currentContext = contexts.get(id);
        if (currentContext == null) {
            throw new IllegalArgumentException(String.format("No context with id '%s' exists.", id));
        }

        setCurrent(currentContext);
    }

    @Override
    public DebuggeeContext get() {
        return current;
    }

    @Override
    public DebuggeeContext createContext(Path execPath, String[] args, Executable executable) throws UdiException
    {
        DebuggeeContextImpl context = new DebuggeeContextImpl();

        context.setEnv(env == null ? null : new HashMap<>(env));
        context.setRootDir(rootDir);
        context.setExecPath(execPath);
        context.setArgs(args);
        context.setExecutable(executable);

        UdiProcess process = udiProcessManager.createProcess(context.getExecPath(),
                context.getArgs(),
                context.getEnv(),
                context.createProcessConfig());

        context.setProcess(process);
        context.setCurrentThread(process.getInitialThread());

        contexts.put(currentId.getAndIncrement(), context);
        setCurrent(context);

        return context;
    }

    private void deleteContext(Integer contextId) {
        synchronized (contexts) {
            if (contextId != null) {
                DebuggeeContext context = contexts.remove(contextId);
                if (context.isActive()) {
                    // Set the current context to any context
                    Collection<DebuggeeContext> values = contexts.values();

                    DebuggeeContext newContext;
                    if (values.size() > 0) {
                        newContext = values.iterator().next();
                    }else{
                        newContext = null;
                    }
                    setCurrent(newContext);
                }
            }
        }
    }

    @Override
    public void deleteContext(DebuggeeContext context) {
        synchronized (contexts) {
            Integer contextId = null;
            for (Map.Entry<Integer, DebuggeeContext> entry : contexts.entrySet()) {
                if (entry.getValue().equals(context)) {
                    contextId = entry.getKey();
                    break;
                }
            }
            deleteContext(contextId);
        }
    }

    @Override
    public void deleteContext(UdiProcess process) {
        synchronized (contexts) {
            Integer contextId = null;
            for (Map.Entry<Integer, DebuggeeContext> entry : contexts.entrySet()) {
                if (entry.getValue().getProcess().equals(process)) {
                    contextId = entry.getKey();
                    break;
                }
            }

            deleteContext(contextId);
        }
    }

    /**
     * @return the processes in this manager that could possibly deliver events
     */
    public List<UdiProcess> getEventProcesses() {
        List<UdiProcess> processes = new ArrayList<>();
        synchronized (contexts) {
            for (DebuggeeContext context : contexts.values()) {
                if (!context.getProcess().isTerminated() && context.getProcess().isRunning()) {
                    processes.add(context.getProcess());
                }
            }
        }

        return processes;
    }

    public List<DebuggeeContext> getContexts() {
        return new ArrayList<>(contexts.values());
    }
}
