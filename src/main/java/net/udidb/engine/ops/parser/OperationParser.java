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

package net.udidb.engine.ops.parser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
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

    private static final String WHITE_SPACE = "\\s+";

    private final Injector injector;

    @Inject
    OperationParser(Injector injector, @Named("OP_IMPL_PACKAGE") String opImplPackage, @Named("CUSTOM_IMPL_PACKAGES") String[] customPackages) {
        this.injector = injector;
        addSupportedOperations(opImplPackage, customPackages);
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
    public Operation parse(String opString) throws UnknownOperationException, OperationParseException {

        // Determine the operation
        String[] tokens = opString.split(WHITE_SPACE);

        Class<? extends Operation > opClass = null;
        String opName = null;
        String[] operandValues = new String[0];
        if (tokens.length > 0) {
            // Look up the hierarchical operation
            StringBuilder opNameBuilder = new StringBuilder();

            int operationSplit = 0;
            opNameBuilder.append(tokens[operationSplit]);
            for (; operationSplit < tokens.length; ++operationSplit) {
                opClass = operations.get(opNameBuilder.toString());
                if (opClass != null) break;
            }
            opName = opNameBuilder.toString();

            operationSplit++;

            if (operationSplit < tokens.length) {
                operandValues = Arrays.copyOfRange(tokens, operationSplit, tokens.length);
            }
        }

        if (opClass == null) {
            throw new UnknownOperationException(String.format("Cannot parse operation from line '%s'", opString));
        }

        Operation cmd;
        try {
            cmd = injector.getInstance(opClass);
        }catch (ConfigurationException | ProvisionException e) {
            throw new UnknownOperationException(String.format("Failed to configure operation '%s'", opName));
        }

        int requiredOperands = 0;
        int restOfLineIndex = -1;
        Map<Integer, Field> operands = new HashMap<>();
        for (Field field : opClass.getDeclaredFields()) {
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

        if (requiredOperands != 0 && operandValues.length == 0) {
            throw new OperationParseException(String.format("Operands required for operation '%s'.",
                    opName));
        }

        if (operandValues.length < requiredOperands || operandValues.length > operands.size()) {
            throw new OperationParseException(String.format("Invalid number of operands. Expected at least %s.",
                    operands.size()));
        }

        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < operandValues.length; ++i) {
            if (i == restOfLineIndex) {
                properties.put(operands.get(i).getName(), Arrays.copyOfRange(operandValues, i, operandValues.length));
                break;
            }else{
                properties.put(operands.get(i).getName(), operandValues[i]);
            }
        }

        try {
            BeanUtils.populate(cmd, properties);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new OperationParseException(String.format("Failed to construct %s operation", opName), e);
        }

        return cmd;
    }
}
