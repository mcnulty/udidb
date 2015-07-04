/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.ops.OperationException;
import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.OperationDescriptionModel;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.ProcessModel;
import net.udidb.server.api.models.ThreadModel;
import net.udidb.server.driver.ServerModule;

/**
 * Explicit singleton for the ServerEngine to allow the instance to be referenced by the REST API whose object lifetimes
 * are managed by Jersey.
 *
 * @author mcnulty
 */
@Singleton
public class ServerEngineImpl implements ServerEngine
{
    private static final Logger logger = LoggerFactory.getLogger(ServerEngineImpl.class);

    private final DebuggeeContextManager debuggeeContextManager;

    @Inject
    public ServerEngineImpl(DebuggeeContextManager debuggeeContextManager) {

        this.debuggeeContextManager = debuggeeContextManager;
    }

    @Override
    public List<DebuggeeContextModel> getDebuggeeContexts() throws OperationException
    {
        return debuggeeContextManager.getContexts().values().stream()
                .map(DebuggeeContextModel::new)
                .collect(Collectors.toList());
    }

    @Override
    public DebuggeeContextModel getDebuggeeContext(String id) throws OperationException
    {
        return new DebuggeeContextModel();
    }

    @Override
    public DebuggeeContextModel createDebuggeeContext(DebuggeeConfigModel config) throws OperationException
    {
        return new DebuggeeContextModel();
    }

    @Override
    public ProcessModel getProcess(String id) throws OperationException
    {
        return new ProcessModel();
    }

    @Override
    public List<ThreadModel> getThreads(String id) throws OperationException
    {
        return new LinkedList<>();
    }

    @Override
    public ThreadModel getThread(String id, String threadId) throws OperationException
    {
        return new ThreadModel();
    }

    @Override
    public OperationModel createOperation(String id, OperationModel operation) throws OperationException
    {
        return new OperationModel();
    }

    @Override
    public OperationModel getOperation(String id) throws OperationException
    {
        return new OperationModel();
    }

    @Override
    public List<OperationDescriptionModel> getOperationDescriptions(String id) throws OperationException
    {
        return new LinkedList<>();
    }
}
