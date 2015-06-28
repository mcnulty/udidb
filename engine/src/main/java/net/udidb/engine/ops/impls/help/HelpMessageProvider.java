/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.help;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;

/**
 * A class that provides access to help messages for operations
 *
 * @author mcnulty
 */
@Singleton
public class HelpMessageProvider {

    private static final String NEWLINE = System.lineSeparator();

    private final Map<String, HelpMessages> helpMessages = new TreeMap<>();

    private static class HelpMessages {
        public String shortMessage;

        public String longMessage;
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

            HelpMessage helpMessageAnnotation = opClass.getAnnotation(HelpMessage.class);
            DisplayName displayName = opClass.getAnnotation(DisplayName.class);
            LongHelpMessage longHelpMessageAnnotation = opClass.getAnnotation(LongHelpMessage.class);

            if (helpMessageAnnotation == null || displayName == null) {
                throw new RuntimeException(opClass.getSimpleName() + " is an invalid Operation");
            }

            String name;
            if (displayName == null) {
                name = "invalid<" + opClass.getSimpleName() + ">";
            }else{
                name = displayName.value();
            }

            HelpMessages messages = new HelpMessages();
            if (helpMessageAnnotation == null) {
                messages.shortMessage = "unspecified help message";
            }else{
                // TODO select message based on locale
                messages.shortMessage = helpMessageAnnotation.enMessage();
            }

            if (longHelpMessageAnnotation == null) {
                messages.longMessage = "unspecified detailed help message";
            }else{
                // TODO select message based on locale
                messages.longMessage = longHelpMessageAnnotation.enMessage().replaceAll("\n", NEWLINE);
            }

            helpMessages.put(name, messages);
        }
    }

    /**
     * @param opName the Operation value
     * @return the short message; null if the operation is unknown
     */
    public String getShortMessage(String opName) {
        HelpMessages messages = helpMessages.get(opName);
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
        HelpMessages messages = helpMessages.get(opName);
        if ( messages != null ) {
            return messages.longMessage;
        }

        return null;
    }

    /**
     * Appends the short message for all register commands, one per line
     *
     * @param builder the StringBuilder to append to
     */
    public void getAllShortMessages(StringBuilder builder) {
        Iterator<Map.Entry<String, HelpMessages>> iter = helpMessages.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, HelpMessages> entry = iter.next();

            builder.append(entry.getKey()).append(" -- ").append(entry.getValue().shortMessage);

            if (iter.hasNext()) {
                builder.append(NEWLINE);
            }
        }
    }
}
