/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import net.libudi.api.event.UdiEvent;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.results.Result;
import net.udidb.server.api.models.OperationModel;

/**
 * @author mcnulty
 */
@Singleton
public class ServerState
{
    private static final Logger logger = LoggerFactory.getLogger(ServerState.class);

    private final Map<String, OperationModel> contextsToModels = new HashMap<>();
    private final Map<Integer, String> operationsToContexts = new HashMap<>();
    private final DebuggeeContextManager debuggeeContextManager;

    @Inject
    public ServerState(DebuggeeContextManager debuggeeContextManager)
    {
        this.debuggeeContextManager = debuggeeContextManager;
    }

    public synchronized OperationModel getOperationModel(DebuggeeContext context)
    {
        return contextsToModels.get(context.getId());
    }

    public synchronized void registerPendingOperation(DebuggeeContext context,
                                                      Operation operation,
                                                      OperationModel model)
    {
        contextsToModels.put(context.getId(), model);
        operationsToContexts.put(System.identityHashCode(operation), context.getId());
    }

    public synchronized void updateModel(Operation op, Result result, String description)
    {
        String contextId = operationsToContexts.get(System.identityHashCode(op));
        OperationModel model;
        if (contextId != null) {
            model = contextsToModels.get(contextId);
        }else{
            model = null;
        }

        if (model == null) {
            // This indicates a programming error
            logger.error("Failed to locate state for operation {} when processing result {}", op.getName(),
                    result.getClass().getSimpleName());
        }else{
            model.setResult(result);
            logger.debug("Debuggee[{}]: operation {}, result '{}'",
                    contextId,
                    op.getName(),
                    description);
        }
    }

    public synchronized String getContextId(Operation op)
    {
        return operationsToContexts.get(System.identityHashCode(op));
    }

    public synchronized void completePendingOperation(UdiEvent event)
    {
        DebuggeeContext debuggeeContext = debuggeeContextManager.getContext(event.getProcess());
        OperationModel opModel = contextsToModels.get(debuggeeContext.getId());
        opModel.setPending(false);
    }
}
