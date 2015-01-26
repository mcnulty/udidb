/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.util;

/**
 * Provides a type-safe copy method for an object
 *
 * @author mcnulty
 */
public interface Copy<T> {

    /**
     * @return a deep-copy of this object
     */
    T copy();
}
