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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import net.udidb.engine.ops.OperationException;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.ModelContainer;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.engine.ServerEngine;

/**
 * @author mcnulty
 */
@Path("/debuggeeContexts")
public class DebuggeeContexts
{
    private static final Logger logger = LoggerFactory.getLogger(DebuggeeContexts.class);

    private final ObjectMapper objectMapper;

    private final ServerEngine serverEngine;

    @Inject
    DebuggeeContexts(ObjectMapper objectMapper, ServerEngine serverEngine)
    {
        this.objectMapper = objectMapper;
        this.serverEngine = serverEngine;
    }

    @GET @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll()
    {
        try {
            return success(new ModelContainer<>(serverEngine.getDebuggeeContexts()));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @POST @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(String body)
    {
        DebuggeeConfigModel config;
        try {
            config = objectMapper.readValue(body, DebuggeeConfigModel.class);
        }catch (JsonProcessingException e) {
            return invalidJsonResponse(e);
        }catch (IOException e) {
            return generalFailure(e);
        }

        try {
            return success(serverEngine.createDebuggeeContext(config));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("{id}") String id)
    {
        try {
            return success(serverEngine.getDebuggeeContext(id));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("/{id}/process")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProcess(@PathParam("{id}") String id)
    {
        try {
            return success(serverEngine.getProcess(id));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("/{id}/process/threads")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThreads(@PathParam("{id}") String id)
    {
        try {
            return success(new ModelContainer<>(serverEngine.getThreads(id)));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("/{id}/process/threads/{threadId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThread(@PathParam("{id}") String id, @PathParam("{threadId}") String threadId)
    {
        try {
            return success(serverEngine.getThread(id, threadId));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @POST
    @Path("/{id}/process/operation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOperation(@PathParam("id") String id, String body)
    {
        OperationModel operation;
        try {
            operation = objectMapper.readValue(body, OperationModel.class);
        }catch (JsonProcessingException e) {
            return invalidJsonResponse(e);
        }catch (IOException e) {
            return generalFailure(e);
        }

        try {
            return success(serverEngine.executeOperation(id, operation));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("/{id}/process/operation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOperation(@PathParam("id") String id)
    {
        try {
            return success(serverEngine.getOperation(id));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("{id}/process/operations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOperationDescriptions(@PathParam("id") String id) throws JsonProcessingException
    {
        try {
            return success(new ModelContainer<>(serverEngine.getOperationDescriptions(id)));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @GET @Path("/operations")
    @Produces(MediaType. APPLICATION_JSON)
    public Response getOperationDescriptions() throws JsonProcessingException
    {
        try {
            return success(new ModelContainer<>(serverEngine.getOperationDescriptions()));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    @POST
    @Path("/globalOperation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGlobalOperation(String body)
    {
        OperationModel operation;
        try {
            operation = objectMapper.readValue(body, OperationModel.class);
        }catch (JsonProcessingException e) {
            return invalidJsonResponse(e);
        }catch (IOException e) {
            return generalFailure(e);
        }

        try {
            return success(serverEngine.executeGlobalOperation(operation));
        }catch (OperationException e) {
            return generalFailure(e);
        }
    }

    private Response success(Object o) {
        try {
            if (o != null) {
                return Response.ok(objectMapper.writeValueAsString(o)).build();
            }else{
                return Response.status(Status.NOT_FOUND).build();
            }
        }catch (JsonProcessingException e) {
            return generalFailure(e);
        }
    }

    private Response generalFailure(Exception e) {
        logger.debug("Failed to produce valid response", e);
        return Response.serverError().build();
    }

    private Response invalidJsonResponse(JsonProcessingException e) {
        logger.debug("Invalid JSON", e);
        return Response.status(INVALID_ENTITY).build();
    }

    private static final StatusType INVALID_ENTITY = new StatusType()
    {

        @Override
        public int getStatusCode()
        {
            return 422;
        }

        @Override
        public Family getFamily()
        {
            return Family.CLIENT_ERROR;
        }

        @Override
        public String getReasonPhrase()
        {
            return "Unprocessable Entity";
        }
    };
}
