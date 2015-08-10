/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test.events;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.udidb.server.driver.EventsSocket;
import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.WampMessages;
import ws.wamp.jawampa.WampMessages.WampMessage;
import ws.wamp.jawampa.WampSerialization;
import ws.wamp.jawampa.connection.IWampConnection;
import ws.wamp.jawampa.connection.IWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnectionPromise;

/**
 * @author mcnulty
 */
public class JettyWampConnection implements IWampConnection, WebSocketListener
{
    private static final Logger logger = LoggerFactory.getLogger(JettyWampConnection.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final IWampConnectionListener connectionListener;
    private Session session;

    public JettyWampConnection(IWampConnectionListener connectionListener)
    {
        this.connectionListener = connectionListener;
    }

    @Override
    public WampSerialization serialization()
    {
        return WampSerialization.Json;
    }

    @Override
    public boolean isSingleWriteOnly()
    {
        return false;
    }

    @Override
    public void sendMessage(WampMessage message, IWampConnectionPromise<Void> promise)
    {
        try {
            session.getRemote().sendString(objectMapper.writeValueAsString(message.toObjectArray(objectMapper)),
                    new WriteCallback()
                    {

                        @Override
                        public void writeFailed(Throwable x)
                        {
                            promise.reject(x);
                        }

                        @Override
                        public void writeSuccess()
                        {
                            promise.fulfill(null);
                        }
                    });
        }catch (WampError | JsonProcessingException e) {
            promise.reject(e);
        }
    }

    @Override
    public void close(boolean sendRemaining, IWampConnectionPromise<Void> promise)
    {
        session.close();
        promise.fulfill(null);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        throw new IllegalArgumentException("Binary messages are not supported by this client");
    }

    @Override
    public void onWebSocketText(String message)
    {
        EventsSocket.passMessageToWampListener(message, objectMapper, connectionListener);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        logger.debug("Connection closed with status = {}, reason = {}", statusCode, reason);
        connectionListener.transportClosed();
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        this.session = session;
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        connectionListener.transportError(cause);
    }
}
