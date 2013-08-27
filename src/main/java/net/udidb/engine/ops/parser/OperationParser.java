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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
public class OperationParser {

    private final Map<String, Class<? extends Operation>> operations = new HashMap<>();

    private static final Pattern OPERATION_PATTERN = Pattern.compile("^(\\S+)\\s*(.*)$");

    private static final String WHITE_SPACE = "\\s+";

    private static final Injector injector = Guice.createInjector(new ParserModule());

    @Inject
    OperationParser(@Named("OP_IMPL_PACKAGE") String opImplPackage) {
        addSupportedOperations(opImplPackage);
    }

    private void addSupportedOperations(String opImplPackage) {
        Reflections reflections = new Reflections(ClasspathHelper.forPackage(opImplPackage),
                new SubTypesScanner());
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
     * @throws OperationParseException if their is an error parsing a knonw operation
     */
    public Operation parse(String opString) throws UnknownOperationException, OperationParseException {

        Matcher matcher = OPERATION_PATTERN.matcher(opString);
        if (matcher.matches()) {
            String opName = matcher.group(1);
            String operandString = matcher.group(2);

            Class<? extends Operation> opClass = operations.get(opName);
            if ( opClass == null ) {
                throw new UnknownOperationException(String.format("Unknown operation '%s'", opName));
            }

            // TODO catch Guice runtime exceptions
            Operation cmd = injector.getInstance(opClass);

            int requiredOperands = 0;
            Map<Integer, Field> operands = new HashMap<>();
            for (Field field : opClass.getDeclaredFields()) {
                Operand operand = field.getAnnotation(Operand.class);
                if (operand != null) {
                    if (!operand.optional()) {
                        requiredOperands++;
                    }

                    if ( operands.put(operand.order(), field) != null ) {
                        throw new OperationParseException(String.format("Order for operand '%s' is not unique.",
                                field.getName()));
                    }
                }
            }

            if (requiredOperands != 0 && operandString.isEmpty()) {
                throw new OperationParseException(String.format("Operands required for operation '%s'.",
                        opName));
            }

            String[] operandValues;
            if (operandString.isEmpty()) {
                operandValues = new String[0];
            }else{
                operandValues = operandString.split(WHITE_SPACE);
            }
            if (operandValues.length < requiredOperands || operandValues.length > operands.size()) {
                throw new OperationParseException(String.format("Invalid number of operands. Expected at least %s.",
                        operands.size()));
            }

            Map<String, Object> properties = new HashMap<>();
            for (int i = 0; i < operandValues.length; ++i) {
                properties.put(operands.get(i).getName(), operandValues[i]);
            }

            try {
                BeanUtils.populate(cmd, properties);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new OperationParseException(String.format("Failed to construct %s operation", opName), e);
            }

            return cmd;
        }else{
            throw new UnknownOperationException(String.format("Cannot parse operation from line '%s'", opString));
        }

    }
}
