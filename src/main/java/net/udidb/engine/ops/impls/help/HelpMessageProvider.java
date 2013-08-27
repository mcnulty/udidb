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

package net.udidb.engine.ops.impls.help;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
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
    HelpMessageProvider(@Named("OP_IMPL_PACKAGE") String opImplPackage) {
        Reflections reflections = new Reflections(ClasspathHelper.forPackage(opImplPackage),
                new SubTypesScanner());
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
