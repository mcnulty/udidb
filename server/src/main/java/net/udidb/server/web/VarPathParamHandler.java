/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author mcnulty
 */
public class VarPathParamHandler implements Handler<RoutingContext>
{
    private final BiConsumer<HttpServerResponse, List<String>> wrapped;
    private final List<String> paramNames;

    public VarPathParamHandler(BiConsumer<HttpServerResponse, List<String>> wrapped, String paramName, String...paramNames)
    {
        this.wrapped = wrapped;
        this.paramNames = new ArrayList<>(1 + paramNames.length);
        this.paramNames.add(paramName);
        Arrays.stream(paramNames).forEach(this.paramNames::add);
    }

    @Override
    public void handle(RoutingContext context)
    {
        List<String> paramValues = new ArrayList<>(paramNames.size());
        for (String paramName : paramNames)
        {
            String paramValue = context.request().params().get(paramName);
            if (paramValue == null)
            {
                context.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
                return;
            }
            paramValues.add(paramValue);
        }

        wrapped.accept(context.response(), paramValues);
    }
}
