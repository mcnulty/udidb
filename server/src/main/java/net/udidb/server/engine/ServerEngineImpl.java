/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.libudi.api.UdiThread;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.OperationProvider;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.impls.control.CreateDebuggee;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.OperationDescription;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.ProcessModel;
import net.udidb.server.api.models.ThreadModel;

/**
 * Default implementation of ServerEngine
 *
 * @author mcnulty
 */
@Singleton
public class ServerEngineImpl implements ServerEngine
{
    private static final Logger logger = LoggerFactory.getLogger(ServerEngineImpl.class);

    private static final String CREATE_OP_NAME = CreateDebuggee.class.getAnnotation(DisplayName.class).value();

    private final DebuggeeContextManager debuggeeContextManager;
    private final OperationProvider operationProvider;
    private final Injector injector;
    private final OperationEngine operationEngine;

    @Inject
    public ServerEngineImpl(Injector injector,
                            DebuggeeContextManager debuggeeContextManager,
                            OperationProvider operationProvider,
                            OperationEngine operationEngine)
    {
        this.injector = injector;
        this.debuggeeContextManager = debuggeeContextManager;
        this.operationProvider = operationProvider;
        this.operationEngine = operationEngine;
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
        DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(id);
        if (debuggeeContext != null) {
            synchronized (debuggeeContext) {
                return new DebuggeeContextModel(debuggeeContext);
            }
        }
        return null;
    }

    @Override
    public ProcessModel getProcess(String id) throws OperationException
    {
        DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(id);
        if (debuggeeContext != null) {
            synchronized (debuggeeContext) {
                return new ProcessModel(debuggeeContext.getProcess());
            }
        }
        return null;
    }

    @Override
    public List<ThreadModel> getThreads(String id) throws OperationException
    {
        DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(id);
        if (debuggeeContext != null) {
            synchronized (debuggeeContext) {
                UdiThread t = debuggeeContext.getProcess().getInitialThread();
                List<ThreadModel> results = new LinkedList<>();
                while (t != null) {
                    results.add(new ThreadModel(t));
                    t = t.getNextThread();
                }
            }
        }
        return Collections.<ThreadModel>emptyList();
    }

    @Override
    public ThreadModel getThread(String id, String threadId) throws OperationException
    {
        DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(id);
        if (debuggeeContext != null) {
            synchronized (debuggeeContext) {
                UdiThread t = debuggeeContext.getProcess().getInitialThread();
                while (t != null) {
                    if (Long.valueOf(t.getTid()).equals(threadId)) {
                        return new ThreadModel(t);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public DebuggeeContextModel createDebuggeeContext(DebuggeeConfigModel config) throws OperationException
    {
        OperationModel resultModel = operationEngine.execute(config.createOperationModel(), null);
        if (resultModel.getResult() instanceof ValueResult) {
            String contextId = ((ValueResult) resultModel.getResult()).getValue().toString();
            DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(contextId);
            if (debuggeeContext == null) {
                logger.error("Did not find debuggee context for newly created process");
            }else {
                return new DebuggeeContextModel(debuggeeContext);
            }
        }else{
            logger.error("Unexpected result returned from create operation");
        }

        return null;
    }

    @Override
    public OperationModel executeOperation(String id, OperationModel operation) throws OperationException
    {
        if (!operation.getName().equals(CREATE_OP_NAME)) {
            DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(id);
            if (debuggeeContext != null) {
                synchronized (debuggeeContext) {
                    return operationEngine.execute(operation, debuggeeContext);
                }
            }
        }else{
            logger.error("The create operation cannot be applied to an existing debuggee");
        }

        return null;
    }

    @Override
    public OperationModel executeGlobalOperation(OperationModel operationModel) throws OperationException
    {
        switch (operationModel.getName()) {
            case "help":
                return operationEngine.execute(operationModel, null);
            default:
                logger.error("The operation {} cannot be executed without a context", operationModel.getName());
                return null;
        }
    }

    @Override
    public OperationModel getOperation(String id) throws OperationException
    {
        DebuggeeContext debuggeeContext = debuggeeContextManager.getContexts().get(id);
        if (debuggeeContext != null) {
            synchronized (debuggeeContext) {
                return operationEngine.get(debuggeeContext);
            }
        }

        return null;
    }

    @Override
    public List<OperationDescription> getOperationDescriptions(String id) throws OperationException
    {
        // Currently, all Operations are available for all debuggees. This could change as language support is added.
        // Therefore, id is currently unused but that might change.
        return getOperationDescriptions();
    }

    @Override
    public List<OperationDescription> getOperationDescriptions() throws OperationException
    {
        List<OperationDescription> results = new LinkedList<>();
        for (Class<? extends Operation> opClass : operationProvider.getOperations().values()) {
            try {
                results.add(OperationDescription.create(injector, opClass));
            }catch (OperationParseException e) {
                logger.warn("Failed to create description of an operation", e);
            }
        }
        return results;
    }
}
