/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.reflections.ReflectionUtils;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.annotations.Operand;

import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * @author mcnulty
 */
public final class OperationDescription
{
    private static final Map<String, OperationDescription> modelCache = new HashMap<>();

    private String name;

    private List<OperandDescription> operandDescriptions;

    public OperationDescription()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<OperandDescription> getOperandDescriptions()
    {
        return operandDescriptions;
    }

    public void setOperandDescriptions(List<OperandDescription> operandDescriptions)
    {
        this.operandDescriptions = operandDescriptions;
    }

    public static OperationDescription create(Injector injector, Class<? extends Operation> opClass)
            throws OperationParseException
    {
        try {
            Operation operation = injector.getInstance(opClass);

            OperationDescription model;
            synchronized (modelCache) {
                model = modelCache.get(operation.getName());
                if (model == null) {
                    model = new OperationDescription();
                    model.setName(operation.getName());

                    List<OperandDescription> operandDescriptions = new LinkedList<>();
                    for (Field field : ReflectionUtils.getAllFields(opClass, withAnnotation(Operand.class))) {
                        Operand operand = field.getAnnotation(Operand.class);
                        if (operand != null) {
                            OperandDescription operandDescription = new OperandDescription();
                            operandDescription.setName(field.getName());

                            if (operand.restOfLine()) {
                                operandDescription.setType(OperandDescription.LIST_TYPE);
                            }else{
                                operandDescription.setType(OperandDescription.STRING_TYPE);
                            }

                            operandDescriptions.add(operandDescription);
                        }
                    }
                    model.setOperandDescriptions(operandDescriptions);
                    modelCache.put(operation.getName(), model);
                }
            }
            return model;
        }catch (ConfigurationException | ProvisionException e) {
            throw new OperationParseException(e);
        }
    }
}
