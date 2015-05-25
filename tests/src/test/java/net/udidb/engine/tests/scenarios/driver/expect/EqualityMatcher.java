/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver.expect;

import static org.junit.Assert.assertEquals;

/**
 * @author mcnulty
 */
public class EqualityMatcher implements ValueMatcher
{
    private final Object value;

    public EqualityMatcher(Object value)
    {
        this.value = value;
    }

    @Override
    public void matches(Object value) throws AssertionError
    {
        assertEquals(this.value, value);
    }
}
