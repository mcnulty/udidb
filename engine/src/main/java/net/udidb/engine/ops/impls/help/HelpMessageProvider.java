/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.help;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.GlobalOperation;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.HelpMessages;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.expr.Expression;

/**
 * A class that provides access to help messages for operations
 *
 * @author mcnulty
 */
@Singleton
public class HelpMessageProvider {

    private static final String NEWLINE = System.lineSeparator();

    private final Map<String, HelpMessageDescriptor> helpMessageDescriptors = new TreeMap<>();

    private final Map<String, Class<?>> operandTypeHelpMixins =
            ImmutableMap.<String, Class<?>>builder()
                        .put(Expression.class.getCanonicalName(), ExpressionHelpMixIn.class)
                        .build();

    private static class HelpMessageDescriptor
    {
        public String shortMessage;

        public String longMessage;

        public boolean global;
    }

    @Inject
    public HelpMessageProvider(@Named("OP_PACKAGES") String[] opPackages) {

        Set<URL> packages = new HashSet<>();
        for (String opPackage : opPackages) {
            packages.addAll(ClasspathHelper.forPackage(opPackage));
        }
        Reflections reflections = new Reflections(packages, new SubTypesScanner());
        for (Class<? extends Operation> opClass : reflections.getSubTypesOf(Operation.class)) {
            if (Modifier.isAbstract(opClass.getModifiers())) continue;

            HelpMessage[] helpMessages = opClass.getAnnotationsByType(HelpMessage.class);
            DisplayName displayName = opClass.getAnnotation(DisplayName.class);

            if (helpMessages.length == 0 || displayName == null) {
                throw new RuntimeException(opClass.getSimpleName() + " is an invalid Operation");
            }

            String name = displayName.value();

            HelpMessageDescriptor descriptor = new HelpMessageDescriptor();

            descriptor.global = opClass.isAnnotationPresent(GlobalOperation.class);
            descriptor.shortMessage = selectMessage(helpMessages);
            descriptor.longMessage = createLongMessage(name, descriptor.shortMessage, opClass);

            helpMessageDescriptors.put(name, descriptor);
        }
    }

    private static String selectMessage(HelpMessage[] helpMessages)
    {

        HelpMessage helpMessage = HelpMessages.Helpers.fromLocale(helpMessages, Locale.getDefault());
        if (helpMessage == null) {
            return "missing help message for Locale " + Locale.getDefault();
        }

        return helpMessage.value();
    }

    private String createLongMessage(String name, String shortMessage, Class<? extends Operation> opClass)
    {
        List<Pair<Field, Operand>> operands =
                ReflectionUtils.getAllFields(opClass, ReflectionUtils.withAnnotation(Operand.class))
                               .stream()
                               .map(f -> Pair.of(f, f.getAnnotation(Operand.class)))
                               .filter(p -> p.getRight() != null)
                               .sorted((p1, p2) -> Integer.compare(p1.getRight().order(), p2.getRight().order()))
                               .collect(Collectors.toList());

        String header = name + " " + operands.stream()
                                             .map(p -> getOperandIdentifier(p.getLeft(), p.getRight()))
                                             .collect(Collectors.joining(" "));

        String operandDescriptions = operands.stream()
                                             .map(p -> String.format("%15s -- %s",
                                                     p.getLeft().getName(),
                                                     getOperandDescription(p.getLeft())))
                                             .collect(Collectors.joining("\n"));

        return header + "\n\n" + shortMessage + "\n\n" + operandDescriptions;
    }

    private String getOperandDescription(Field field)
    {
        HelpMessage[] helpMessages = field.getAnnotationsByType(HelpMessage.class);
        String message;
        if (helpMessages.length != 0) {
            message = selectMessage(helpMessages);
        }else{
            message = null;
        }

        if (message == null) {
            Class<?> mixInClass = operandTypeHelpMixins.get(field.getType().getCanonicalName());
            if (mixInClass != null) {
                HelpMessage[] mixInMessages = mixInClass.getAnnotationsByType(HelpMessage.class);
                message = selectMessage(mixInMessages);
            }
        }

        return message;
    }

    private String getOperandIdentifier(Field field, Operand operand)
    {
        String identifier;
        if (operand.restOfLine()) {
            identifier = field.getName() + "...";
        }else{
            identifier = field.getName();
        }

        if (operand.optional())
        {
            return "[" + identifier + "]";
        }

        return "<" + identifier + ">";
    }

    /**
     * @param opName the Operation value
     * @return the short message; null if the operation is unknown
     */
    public String getShortMessage(String opName) {
        HelpMessageDescriptor messages = helpMessageDescriptors.get(opName);
        if ( messages != null ) {
            return messages.shortMessage;
        }

        return null;
    }

    /**
     * @param opName the Operation value
     * @return the long message; null if the operation is unknown
     */
    public String getLongMessage(String opName) {
        HelpMessageDescriptor messages = helpMessageDescriptors.get(opName);
        if ( messages != null ) {
            return messages.longMessage;
        }

        return null;
    }

    public String getGlobalLongMessage(String opName) {
         HelpMessageDescriptor messages = helpMessageDescriptors.get(opName);
        if ( messages != null ) {
            if (messages.global) {
                return messages.longMessage;
            }
        }

        return null;
    }

    /**
     * Appends the short message for all register operations, one per line
     *
     * @param builder the StringBuilder to append to
     */
    public void getAllShortMessages(StringBuilder builder) {
        getAllShortMessages(builder, e -> true);
    }

    private void getAllShortMessages(StringBuilder builder, Predicate<Map.Entry<String, HelpMessageDescriptor>> predicate) {

        boolean first = true;
        for (Map.Entry<String, HelpMessageDescriptor> entry : helpMessageDescriptors.entrySet()) {

            if (!predicate.test(entry)) {
                continue;
            }

            if (!first) {
                builder.append(NEWLINE);
            }else{
                first = false;
            }

            builder.append(entry.getKey()).append(" -- ").append(entry.getValue().shortMessage);
        }
    }

    /**
     * Appends the short message for all registered, global-only operations, one per line
     *
     * @param builder the StringBuilder to append to
     */
    public void getAllGlobalShortMessages(StringBuilder builder) {
        getAllShortMessages(builder, e -> e.getValue().global);
    }
}
