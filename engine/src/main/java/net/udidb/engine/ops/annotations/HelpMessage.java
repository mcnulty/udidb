/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used to provide the help message for using a command
 *
 * @author mcnulty
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HelpMessage {

    /**
     * @return the English help message
     */
    String enMessage();
}
