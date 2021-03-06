/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextAware;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.OperationProvider;
import net.udidb.engine.ops.UnknownOperationException;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.parser.OperandParser;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;
import net.udidb.server.api.models.OperationModel;

import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * @author mcnulty
 */
@Singleton
public final class OperationEngine implements OperationResultVisitor
{
    private static final Logger logger = LoggerFactory.getLogger(OperationEngine.class);

    private final Injector injector;
    private final ServerEventDispatcher eventDispatcher;
    private final Map<String, Class<? extends Operation>> operations;
    private final BeanUtilsBean beanUtils;
    private final ServerState serverState;

    @Inject
    public OperationEngine(Injector injector,
                           OperationProvider operationProvider,
                           ServerEventDispatcher eventDispatcher,
                           ServerState serverState)
    {
        this.injector = injector;
        this.eventDispatcher = eventDispatcher;
        this.operations = operationProvider.getOperations();
        this.beanUtils = BeanUtilsBean.getInstance();
        this.serverState = serverState;
    }

    public OperationModel execute(OperationModel operationModel, DebuggeeContext debuggeeContext)
            throws UnknownOperationException, OperationException
    {
        synchronized (serverState) {
            if (debuggeeContext != null) {
                OperationModel lastOperation = serverState.getOperationModel(debuggeeContext);
                if (lastOperation != null && lastOperation.isPending()) {
                    throw new OperationInProgressException("Operation '" + lastOperation.getName() +
                            "' already in progress for debuggee context '"
                            + debuggeeContext.getId() + "'");
                }
            }

            Operation operation = configureOperation(operationModel, debuggeeContext);

            try {
                OperationModel opModel = new OperationModel(operationModel);
                if (debuggeeContext != null) {
                    serverState.registerPendingOperation(debuggeeContext, operation, opModel);
                }

                Result result = operation.execute();

                if (result.isEventPending()) {
                    opModel.setPending(true);
                    opModel.setResult(null);
                } else {
                    opModel.setResult(result);
                }

                if (debuggeeContext != null) {
                    result.accept(operation, this);
                }

                if (result.isEventPending()) {
                    eventDispatcher.readyForEvent(debuggeeContext);
                }

                return opModel;
            } catch (OperationException e) {
                visit(operation, e);
                throw e;
            } catch (Exception e) {
                visit(operation, e);
                throw new OperationException(e);
            }
        }
    }

    private Operation configureOperation(OperationModel operationModel, DebuggeeContext debuggeeContext)
            throws OperationException
    {
        Class<? extends Operation> opClass = operations.get(operationModel.getName());
        if (opClass == null) {
            throw new UnknownOperationException("Unknown operation: " + operationModel.getName());
        }

        Operation operation = injector.getInstance(opClass);
        if (operation instanceof DebuggeeContextAware) {
            ((DebuggeeContextAware) operation).setDebuggeeContext(debuggeeContext);
        }

        Map<String, Field> fields = new HashMap<>(ReflectionUtils.getAllFields(opClass, withAnnotation(Operand.class))
                .stream().collect(Collectors.toMap(Field::getName, f -> f)));

        Map<String, Object> operands = operationModel.getOperands();

        if (operands != null) {
            for (Map.Entry<String, Object> fieldEntry : operands.entrySet()) {
                Field field = fields.remove(fieldEntry.getKey());
                if (field == null) {
                    throw new OperationParseException("Unknown operation field specified: " + fieldEntry.getKey());
                }

                Operand operand = field.getAnnotation(Operand.class);
                if (operand == null) {
                    throw new OperationParseException("Invalid operand field: " + field.getName());
                }

                OperandParser parser = injector.getInstance(operand.operandParser());

                if (parser instanceof DebuggeeContextAware) {
                    ((DebuggeeContextAware) parser).setDebuggeeContext(debuggeeContext);
                }

                try {
                    if (operand.restOfLine()) {
                        if (fieldEntry.getValue() instanceof List) {
                            beanUtils.copyProperty(operation, field.getName(),
                                    parser.parse((List<String>) fieldEntry.getValue(), field));
                        } else {
                            throw new OperationParseException(fieldEntry.getKey() + " field must be a list");
                        }
                    } else {
                        beanUtils.copyProperty(operation, field.getName(),
                                parser.parse(fieldEntry.getValue().toString(), field));
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new OperationParseException(String.format("Failed to configure operation %s from argument values",
                            fieldEntry.getKey()));
                }
            }
        }

        for (Field field : fields.values()) {
            Operand operand = field.getAnnotation(Operand.class);
            if (operand == null) {
                throw new OperationParseException("Invalid operand field: " + field.getName());
            }

            if (!operand.optional()) {
                throw new OperationParseException("Required field '" + field.getName() + "' not specified");
            }
        }

        return operation;
    }

    @Override
    public boolean visit(Operation op, VoidResult result)
    {
        serverState.updateModel(op, result, "(no result)");
        return true;
    }

    @Override
    public boolean visit(Operation op, ValueResult result)
    {
        serverState.updateModel(op, result, result.getDescription());
        return true;
    }

    @Override
    public boolean visit(Operation op, TableResult result)
    {
        serverState.updateModel(op, result, "\n" + result.toCSV("    "));
        return true;
    }

    @Override
    public boolean visit(Operation op, DeferredResult result)
    {
        String contextId = serverState.getContextId(op);
        logger.debug("Debuggee[{}]: operation {}, deferred result",
                contextId,
                op.getName());
        if (result.getDeferredEventObserver() != null) {
            eventDispatcher.registerEventObserver(result.getDeferredEventObserver());
        }
        return true;
    }

    @Override
    public boolean visit(Operation op, Exception e)
    {
        logger.error("Exception encountered while processing operation " + op.getName(), e);
        return true;
    }
}
