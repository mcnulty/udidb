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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessManager;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.files.Executable;

/**
 * Implementation of DebuggeeContextFactory
 *
 * @author mcnulty
 */
@Singleton
public class DebuggeeContextManagerImpl implements DebuggeeContextManager
{
    private final Map<String, DebuggeeContext> contexts = Collections.synchronizedMap(new HashMap<String, DebuggeeContext>());

    private Path rootDir = Files.createTempDir().toPath();

    // This must be null as a value of null has the environment inherited from the
    // creating process
    private Map<String, String> env = null;

    private final AtomicInteger currentId = new AtomicInteger(0);

    private final UdiProcessManager udiProcessManager;

    private final List<DebuggeeContextObserver> observers = Collections.synchronizedList(new LinkedList<>());

    @Inject
    DebuggeeContextManagerImpl(UdiProcessManager udiProcessManager) {
        this.udiProcessManager = udiProcessManager;
    }

    public Path getRootDir()
    {
        return rootDir;
    }

    public void setRootDir(Path rootDir)
    {
        this.rootDir = rootDir;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    @Override
    public DebuggeeContext createContext(Path execPath, String[] args, Executable executable) throws UdiException
    {
        return createContext(execPath,
                args,
                env == null ? null : new HashMap<String, String>(env),
                executable);
    }

    @Override
    public DebuggeeContext createContext(Path execPath, String[] args, Map<String, String> env, Executable executable) throws UdiException
    {
        DebuggeeContextImpl context = new DebuggeeContextImpl(currentId.getAndIncrement());

        context.setEnv(env);
        context.setRootDir(rootDir);
        context.setExecPath(execPath);
        context.setArgs(args);
        context.setExecutable(executable);

        UdiProcess process = udiProcessManager.createProcess(context.getExecPath(),
                context.getArgs(),
                context.getEnv(),
                context.createProcessConfig());

        process.setUserData(context);
        context.setProcess(process);
        context.setCurrentThread(process.getInitialThread());

        contexts.put(context.getId(), context);

        notifyCreated(context);

        return context;
    }

    private void notifyCreated(DebuggeeContext created)
    {
        synchronized (observers) {
            observers.stream().forEachOrdered(o -> o.created(created));
        }
    }

    private void notifyDelete(DebuggeeContext deleted)
    {
       synchronized (observers) {
           observers.stream().forEachOrdered(o -> o.deleted(deleted));
       }
    }

    @Override
    public void deleteContext(DebuggeeContext context)
    {
        DebuggeeContext deleted;
        synchronized (contexts) {
            deleted = contexts.remove(context.getId());
        }
        if (deleted != null) {
            notifyDelete(deleted);
        }
    }

    @Override
    public DebuggeeContext deleteContext(UdiProcess process)
    {
        DebuggeeContext deleted = null;
        if (process.getUserData() instanceof DebuggeeContext) {
            deleted = (DebuggeeContext)process.getUserData();
            deleteContext(deleted);
        }else{
            synchronized (contexts) {
                Iterator<Entry<String, DebuggeeContext>> i = contexts.entrySet().iterator();
                while (i.hasNext()) {
                    DebuggeeContext context = i.next().getValue();
                    if (context.getProcess().equals(process)) {
                        i.remove();
                        deleted = context;
                        break;
                    }
                }
            }
        }
        if (deleted != null) {
            notifyDelete(deleted);
        }
        return deleted;
    }

    @Override
    public DebuggeeContext getContext(UdiProcess process)
    {
        if (process.getUserData() instanceof DebuggeeContext) {
            return (DebuggeeContext)process.getUserData();
        }else{
            synchronized (contexts) {
                Iterator<Entry<String, DebuggeeContext>> i = contexts.entrySet().iterator();
                while (i.hasNext()) {
                    DebuggeeContext context = i.next().getValue();
                    if (context.getProcess().equals(process)) {
                        return context;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Map<String, DebuggeeContext> getContexts()
    {
        synchronized (contexts) {
            return new HashMap<>(contexts);
        }
    }

    /**
     * @return the processes in this manager that could possibly deliver events
     */
    @Override
    public List<DebuggeeContext> getEventContexts() {

        synchronized (contexts) {
            return new LinkedList<>(contexts.values().stream()
                    .filter(context -> !context.getProcess().isTerminated() && context.getProcess().isRunning())
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public void addObserver(DebuggeeContextObserver observer)
    {
        observers.add(observer);
    }
}