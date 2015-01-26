/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.parser;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;

/**
 * Converts an address string (in various formats) to a Long
 *
 * @author mcnulty
 */
public class AddressConverter implements Converter {

    @Override
    public Object convert(Class type, Object value) {
        if (long.class.isAssignableFrom(type)) {
            // Try hex first
            try {
                String valueString;
                if (value.toString().startsWith("0x")) {
                    valueString = value.toString().replaceFirst("0x", "");
                }else{
                    valueString = value.toString();
                }
                return Long.parseLong(valueString, 16);
            }catch (NumberFormatException e) {
            }

            // Try decimal second
            try {
                return Long.parseLong(value.toString(), 10);
            }catch (NumberFormatException e) {
            }

            // Try octal
            try {
                return Long.parseLong(value.toString(), 8);
            }catch (NumberFormatException e) {
            }
        }

        throw new ConversionException(String.format("Failed to convert value to convert %s to long", value));
    }
}
