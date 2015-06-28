/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.UnknownOperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.Operand;

import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Given a String representation of an Operation and its Operands, create and populate the Operation
 *
 * TODO allow aliases for operations
 *
 * @author mcnulty
 */
@Singleton
public class OperationParser {

    private final Map<String, Class<? extends Operation>> operations = new HashMap<>();

    private final Injector injector;

    private final BeanUtilsBean beanUtils;

    @Inject
    OperationParser(Injector injector, @Named("OP_IMPL_PACKAGE") String opImplPackage, @Named("CUSTOM_IMPL_PACKAGES") String[] customPackages) {
        this.injector = injector;
        addSupportedOperations(opImplPackage, customPackages);
        beanUtils = BeanUtilsBean.getInstance();
    }

    private void addSupportedOperations(String opImplPackage, String[] customPackages) {
        Set<URL> packages = new HashSet<>();
        packages.addAll(ClasspathHelper.forPackage(opImplPackage));
        for (String pack : customPackages) {
            packages.addAll(ClasspathHelper.forPackage(pack));
        }

        Reflections reflections = new Reflections(packages, new SubTypesScanner());
        for (Class<? extends Operation> opClass : reflections.getSubTypesOf(Operation.class)) {
            if (Modifier.isAbstract(opClass.getModifiers())) continue;

            DisplayName displayName = opClass.getAnnotation(DisplayName.class);
            if (displayName != null ) {
                operations.put(displayName.value(), opClass);
            }else{
                throw new RuntimeException(opClass.getSimpleName() + " is an invalid Operation.");
            }
        }
    }

    /**
     * @param opString the String representing the operation to be performed
     * @return the Operation (never null)
     *
     * @throws UnknownOperationException if the opString references an unknown operation
     * @throws OperationParseException if their is an error parsing a known operation
     */
    public Operation parse(String opString) throws UnknownOperationException, OperationParseException
    {
        final String UNQUOTED_INITIAL_DELIMS = " \t\n\r\f\"";
        final String QUOTED_DELIMS = "\"";

        StringTokenizer tokenizer = new StringTokenizer(opString, UNQUOTED_INITIAL_DELIMS, true);

        String opName = "<unknown>";
        Class<? extends Operation > opClass = null;
        List<String> operandValues = new LinkedList<>();

        boolean firstOpToken = true;
        StringBuilder opNameBuilder = new StringBuilder();

        String delims = UNQUOTED_INITIAL_DELIMS;
        while ( tokenizer.hasMoreTokens() ) {
            String token = tokenizer.nextToken(delims);

            // Is the token a delimiter?
            if (token.length() == 1 && delims.contains(token)) {
                if (QUOTED_DELIMS.contains(token)) {
                    if (delims.equals(UNQUOTED_INITIAL_DELIMS)) {
                        // start of a quoted string
                        delims = QUOTED_DELIMS;
                    }else{
                        // end of a quoted string
                        delims = UNQUOTED_INITIAL_DELIMS;
                    }
                }
                continue;
            }

            if (opClass == null) {
                // Parsing the operation name
                if (!firstOpToken) {
                    opNameBuilder.append(" ");
                }else{
                    firstOpToken = false;
                }
                opNameBuilder.append(token);

                opClass = operations.get(opNameBuilder.toString());
                if (opClass != null) {
                    opName = opNameBuilder.toString();
                }
            }else{
                // Parsing the operands
                operandValues.add(token);
            }
        }

        if (opClass == null) {
            throw new UnknownOperationException(String.format("Cannot parse operation from line '%s'", opString));
        }

        Operation cmd;
        try {
            cmd = injector.getInstance(opClass);
        }catch (ConfigurationException | ProvisionException e) {
            throw new UnknownOperationException(String.format("Failed to configure operation '%s'", opName), e);
        }

        int requiredOperands = 0;
        int restOfLineIndex = -1;
        Map<Integer, Field> operands = new HashMap<>();
        for (Field field : ReflectionUtils.getAllFields(opClass, withAnnotation(Operand.class))) {
            Operand operand = field.getAnnotation(Operand.class);
            if (operand != null) {
                if (operand.order() > restOfLineIndex && restOfLineIndex != -1) {
                    throw new OperationParseException(String.format("Operand '%s' cannot fall after rest of line operand.",
                            field.getName()));
                }

                if (!operand.optional()) {
                    requiredOperands++;
                }

                if (operand.restOfLine()) {
                    if (restOfLineIndex != -1) {
                        throw new OperationParseException("The rest of line property can only be set on one operand.");
                    }

                    restOfLineIndex = operand.order();
                }

                if ( operands.put(operand.order(), field) != null ) {
                    throw new OperationParseException(String.format("Order for operand '%s' is not unique.",
                            field.getName()));
                }
            }
        }

        if (requiredOperands != 0 && operandValues.size() == 0) {
            throw new OperationParseException(String.format("Operands required for operation '%s'.",
                    opName));
        }

        if (operandValues.size() < requiredOperands || (restOfLineIndex == -1 && operandValues.size() > operands.size()) ) {
            throw new OperationParseException(String.format("Invalid number of operands. Expected at least %s.",
                    operands.size()));
        }

        for (int i = 0; i < operandValues.size(); ++i) {
            Field field = operands.get(i);

            OperandParser operandParser = injector.getInstance(field.getAnnotation(Operand.class).operandParser());
            try {
                if (i == restOfLineIndex) {
                    beanUtils.copyProperty(cmd, field.getName(),
                            operandParser.parse(operandValues.subList(i, operandValues.size()), field));
                    break;
                } else {
                    beanUtils.copyProperty(cmd,
                            field.getName(),
                            operandParser.parse(operandValues.get(i), field));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new OperationParseException(String.format("Failed to construct %s operation", opName), e);
            }
        }

        return cmd;
    }
}
