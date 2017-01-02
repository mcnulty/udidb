/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.web;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author mcnulty
 */
public class ExceptionHandler implements Handler<RoutingContext>
{
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private final Handler<RoutingContext> wrapped;

    public ExceptionHandler(Handler<RoutingContext> wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void handle(RoutingContext context)
    {
        try
        {
            wrapped.handle(context);
        }
        catch (Throwable e)
        {
            logger.error("API request failed", e);
            context.response()
                   .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                   .putHeader(Names.CONTENT_TYPE, "text/plain;charset=UTF-8")
                   .end(ExceptionUtils.getStackTrace(e));
        }
    }
}
