/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver.expect;

import static org.junit.Assert.assertTrue;

/**
 * @author mcnulty
 */
public class StringContainsValueMatcher implements ValueMatcher
{
    private final String contains;

    public StringContainsValueMatcher(String contains)
    {
        this.contains = contains;
    }

    @Override
    public void matches(Object value) throws AssertionError
    {
        assertTrue(value.toString().contains(contains));
    }
}
