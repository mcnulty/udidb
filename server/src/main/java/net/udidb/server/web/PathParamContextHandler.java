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
import io.vertx.ext.web.RoutingContext;

/**
 * @author mcnulty
 */
public class PathParamContextHandler implements Handler<RoutingContext>
{
    private final BiConsumer<RoutingContext, String> wrapped;
    private final String paramName;

    public PathParamContextHandler(String paramName, BiConsumer<RoutingContext, String> wrapped)
    {
        this.wrapped = wrapped;
        this.paramName = paramName;
    }

    @Override
    public void handle(RoutingContext context)
    {
        String paramValue = context.request().params().get(paramName);
        if (paramValue == null) {
            context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        } else {
            wrapped.accept(context, paramValue);
        }
    }
}
