/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.resources;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import net.libudi.api.exceptions.RequestException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.ErrorModel;
import net.udidb.server.api.models.ModelContainer;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.engine.ServerEngine;

/**
 * @author mcnulty
 */
@Singleton
public class DebuggeeContexts
{
    private static final String APPLICATION_JSON = "application/json";

    private static final Logger logger = LoggerFactory.getLogger(DebuggeeContexts.class);

    private final ObjectMapper objectMapper;

    private final ServerEngine serverEngine;

    @Inject
    public DebuggeeContexts(ObjectMapper objectMapper, ServerEngine serverEngine)
    {
        this.objectMapper = objectMapper;
        this.serverEngine = serverEngine;
    }

    public void getAll(HttpServerResponse response)
    {
        try {
            success(response, new ModelContainer<>(serverEngine.getDebuggeeContexts()));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void get(HttpServerResponse response, String id)
    {
        try {
            success(response, serverEngine.getDebuggeeContext(id));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void create(HttpServerResponse response, String body)
    {
        DebuggeeConfigModel config;
        try {
            config = objectMapper.readValue(body, DebuggeeConfigModel.class);
            success(response, serverEngine.createDebuggeeContext(config));
        }catch (OperationException e) {
            generalFailure(response, e);
        }catch (JsonProcessingException e) {
            invalidJsonResponse(response, e);
        }catch (IOException e) {
            generalFailure(response, e);
        }
    }

    public void getProcess(HttpServerResponse response, String id)
    {
        try {
            success(response, serverEngine.getProcess(id));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void getThreads(HttpServerResponse response, String id)
    {
        try {
            success(response, new ModelContainer<>(serverEngine.getThreads(id)));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void getThread(HttpServerResponse response, List<String> params)
    {
        String id = params.get(0);
        String threadId = params.get(1);
        try {
            success(response, serverEngine.getThread(id, threadId));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void createOperation(RoutingContext context, String id)
    {
        try {
            OperationModel operation = objectMapper.readValue(context.getBodyAsString(), OperationModel.class);
            success(context.response(), serverEngine.executeOperation(id, operation));
        }catch (OperationParseException e) {
            inputError(context.response(), e);
        }catch (OperationException e) {
            if (e.getCause() instanceof RequestException) {
                inputError(context.response(), (RequestException)e.getCause());
            }else {
                generalFailure(context.response(), e);
            }
        }catch (JsonProcessingException e) {
            invalidJsonResponse(context.response(), e);
        }catch (IOException e) {
            generalFailure(context.response(), e);
        }
    }

    public void getOperation(HttpServerResponse response, String id)
    {
        try {
            success(response, serverEngine.getOperation(id));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void getOperationDescriptions(HttpServerResponse response, String id)
    {
        try {
            success(response, new ModelContainer<>(serverEngine.getOperationDescriptions(id)));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void getOperationDescriptions(HttpServerResponse response)
    {
        try {
            success(response, new ModelContainer<>(serverEngine.getOperationDescriptions()));
        }catch (OperationException e) {
            generalFailure(response, e);
        }
    }

    public void createGlobalOperation(HttpServerResponse response, String body)
    {
        OperationModel operation;
        try {
            operation = objectMapper.readValue(body, OperationModel.class);
            success(response, serverEngine.executeGlobalOperation(operation));
        }catch (OperationParseException e) {
            inputError(response, e);
        }catch (OperationException e) {
            if (e.getCause() instanceof RequestException) {
                inputError(response, (RequestException)e.getCause());
            }else{
                generalFailure(response, e);
            }
        }catch (JsonProcessingException e) {
            invalidJsonResponse(response, e);
        }catch (IOException e) {
            generalFailure(response, e);
        }
    }

    private void success(HttpServerResponse response, Object o) {
        try {
            if (o != null) {
                response.setStatusCode(HttpResponseStatus.OK.code())
                        .putHeader(Names.CONTENT_TYPE, APPLICATION_JSON)
                        .end(objectMapper.writeValueAsString(o));
            }else{
                response.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                        .end();
            }
        }catch (JsonProcessingException e) {
            generalFailureNoBody(response, e);
        }
    }

    private void generalFailure(HttpServerResponse response, Exception e) {
        logger.debug("Failed to produce valid response", e);
        try {
            response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .putHeader(Names.CONTENT_TYPE, APPLICATION_JSON)
                    .end(objectMapper.writeValueAsString(new ErrorModel(e)));
        }catch (JsonProcessingException jsonException) {
            generalFailureNoBody(response, jsonException);
        }
    }

    private void generalFailureNoBody(HttpServerResponse response, Exception e) {
        logger.error("Failed to produce valid response", e);
        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                .end();
    }

    private void inputError(HttpServerResponse response, Exception e) {
        logger.error("Invalid input", e);
        try {
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .putHeader(Names.CONTENT_TYPE, APPLICATION_JSON)
                    .end(objectMapper.writeValueAsString(new ErrorModel(e)));
        }catch (JsonProcessingException jsonException) {
            generalFailureNoBody(response, jsonException);
        }
    }

    private void invalidJsonResponse(HttpServerResponse response, JsonProcessingException e) {
        logger.debug("Invalid JSON", e);
        response.setStatusCode(HttpResponseStatus.UNPROCESSABLE_ENTITY.code())
                .end();
    }
}
