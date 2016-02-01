/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A container for {@link HelpMessage}
 *
 * @author mcnulty
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HelpMessages
{

    /**
     * @return the contained HelpMessage instances
     */
    HelpMessage[] value();

    /**
     * Helper methods for working with HelpMessages
     */
    class Helpers
    {
        private Helpers()
        {
        }

        public static HelpMessage fromLocale(HelpMessage[] helpMessages, Locale locale)
        {
            Map<String, HelpMessage> tags =
                    Stream.of(helpMessages)
                          .collect(Collectors.toMap(HelpMessage::languageTag, Function.identity()));

            String languageRange = locale.toLanguageTag() + ";q=1.0," +
                    locale.getLanguage() + "-" + locale.getCountry() + ";q=0.5," +
                    locale.getLanguage() + ";q=0.1";

            String selectedKey = Locale.lookupTag(Locale.LanguageRange.parse(languageRange), tags.keySet());

            if (selectedKey != null) {
                return tags.get(selectedKey);
            }

            return null;
        }
    }
}
