/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author mcnulty
 */
public interface TableRowMixIn
{
    @JsonIgnore
    List<String> getColumnHeaders();

    List<String> getColumnValues();
}
