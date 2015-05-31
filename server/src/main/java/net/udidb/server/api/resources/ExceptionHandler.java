/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.resources;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.grizzly.utils.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mcnulty
 */
@Provider
public class ExceptionHandler implements ExceptionMapper<Exception>
{
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    // TODO make configurable

    @Override
    public Response toResponse(Exception e)
    {
        logger.debug("API request failed", e);
        return Response.status(500).entity(Exceptions.getStackTraceAsString(e)).type("text/plain")
                .build();
    }
}
