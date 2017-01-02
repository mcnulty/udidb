/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.web;

import java.util.function.BiConsumer;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author mcnulty
 */
public class PathParamHandler implements Handler<RoutingContext>
{
    private final BiConsumer<HttpServerResponse, String> wrapped;
    private final String paramName;

    public PathParamHandler(String paramName, BiConsumer<HttpServerResponse, String> wrapped)
    {
        this.wrapped = wrapped;
        this.paramName = paramName;
    }

    @Override
    public void handle(RoutingContext context)
    {
        String paramValue = context.request().getParam(paramName);
        if (paramValue == null) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        } else {
            wrapped.accept(context.response(), paramValue);
        }
    }
}
