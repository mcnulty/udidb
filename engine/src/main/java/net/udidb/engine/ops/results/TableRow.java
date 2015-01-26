/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.results;

import java.util.List;

/**
 * Provides a means to include an object as a row in a TableResult
 *
 * @author mcnulty
 */
public interface TableRow {

    /**
     * @return gets the columns headers for the table
     */
    List<String> getColumnHeaders();

    /**
     * @return gets the column values for the row represented by this object
     */
    List<String> getColumnValues();
}
