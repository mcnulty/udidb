/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author mcnulty
 */
public enum ObjectMapperInstance
{
    INSTANCE;

    private final ObjectMapper objectMapper;

    private ObjectMapperInstance()
    {
        objectMapper = new ObjectMapper();
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

}
