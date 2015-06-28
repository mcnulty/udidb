/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.ModelContainer;
import net.udidb.server.api.models.OperationDescriptionModel;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.ProcessModel;
import net.udidb.server.api.models.ThreadModel;
import net.udidb.server.api.util.ObjectMapperInstance;

/**
 * @author mcnulty
 */
@Path("/debuggeeContexts")
public class DebuggeeContexts
{
    private static final ObjectMapper objectMapper = ObjectMapperInstance.INSTANCE.getObjectMapper();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAll() throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new ModelContainer<DebuggeeContextModel>());
    }

    @GET @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("{id}") String id) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new DebuggeeContextModel());
    }

    @GET @Path("/{id}/process")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProcess(@PathParam("{id}") String id) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new ProcessModel());
    }

    @GET @Path("/{id}/process/threads")
    @Produces(MediaType.APPLICATION_JSON)
    public String getThreads(@PathParam("{id}") String id) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new ModelContainer<ThreadModel>());
    }

    @GET @Path("/{id}/process/threads/{threadId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getThread(@PathParam("{id}") String id, @PathParam("{threadId}") String threadId) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new ThreadModel());
    }

    @PUT
    @Path("/{id}/process/operation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String createOperation(@PathParam("id") String id) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new OperationModel());
    }

    @GET @Path("/{id}/process/operation")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOperation(@PathParam("id") String id) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new OperationModel());
    }

    @GET @Path("{id}/process/operations")
    @Produces(MediaType.APPLICATION_JSON)
    public String getOperationDescriptions(@PathParam("id") String id) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new ModelContainer<OperationDescriptionModel>());
    }
}
