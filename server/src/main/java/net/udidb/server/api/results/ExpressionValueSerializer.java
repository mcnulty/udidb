/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.results;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import net.udidb.expr.values.ExpressionValue;

/**
 * @author mcnulty
 */
public class ExpressionValueSerializer extends JsonSerializer<ExpressionValue>
{

    @Override
    public void serialize(ExpressionValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException
    {
        switch (value.getType()) {
            case CHAR:
                jgen.writeString(Character.toString(value.getCharValue()));
                break;
            case STRING:
                jgen.writeString(value.getStringValue());
                break;
            case ADDRESS:
                jgen.writeString("0x" + Long.toHexString(value.getAddressValue()));
                break;
            case NUMBER:
                jgen.writeString(value.toString());
                break;
        }
    }
}
