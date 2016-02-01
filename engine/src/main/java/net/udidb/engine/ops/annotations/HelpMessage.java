/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

/**
 * An annotation used to provide the help message for using a command
 *
 * @author mcnulty
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(HelpMessages.class)
public @interface HelpMessage {

    /**
     * @return the language tag that can be passed to {@link Locale.forLanguageTag}
     */
    String languageTag() default "en";

    /**
     * @return the message in the Locale specified by the language tag
     */
    String value();
}
