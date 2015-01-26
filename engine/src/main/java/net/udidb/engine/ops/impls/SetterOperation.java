/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls;

import net.udidb.engine.ops.annotations.Operand;

/**
 * Operation that sets a single value
 *
 * @param <T> the type of the value
 *
 * @author mcnulty
 */
public abstract class SetterOperation<T> extends DisplayNameOperation {

    @Operand(order=0)
    protected T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
