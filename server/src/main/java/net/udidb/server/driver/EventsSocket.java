/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import ws.wamp.jawampa.WampError;
import ws.wamp.jawampa.WampMessages;
import ws.wamp.jawampa.WampMessages.WampMessage;
import ws.wamp.jawampa.WampRouter;
import ws.wamp.jawampa.WampSerialization;
import ws.wamp.jawampa.connection.IWampConnection;
import ws.wamp.jawampa.connection.IWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnectionPromise;

/**
 * @author mcnulty
 */
public class EventsSocket extends WebSocketAdapter implements IWampConnection
{
    private static final Logger logger = LoggerFactory.getLogger(EventsSocket.class);

    private final WampRouter wampRouter;
    private final ObjectMapper objectMapper;

    private IWampConnectionListener connectionListener;

    @Inject
    public EventsSocket(WampRouter wampRouter, ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
        this.wampRouter = wampRouter;
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len)
    {
        throw new IllegalArgumentException("Binary messages are not supported by this server");
    }

    @Override
    public void onWebSocketText(String message)
    {
        passMessageToWampListener(message, objectMapper, connectionListener);
    }

    public static void passMessageToWampListener(String message, ObjectMapper objectMapper, IWampConnectionListener connectionListener)
    {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            if (jsonNode.isArray()) {
                WampMessage wampMessage = WampMessages.WampMessage.fromObjectArray((ArrayNode)jsonNode);
                if (wampMessage == null) {
                    logger.warn("Encountered unknown WAMP message, ignoring: '{}'", message);
                }else{
                    connectionListener.messageReceived(wampMessage);
                }
            }else{
                throw new IOException("Invalid format of message, JSON array expected");
            }
        }catch (IOException | WampError e) {
            // TODO more robust handling of this may be required in the future
            throw new IllegalArgumentException("Failed to parse or process incoming message", e);
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        connectionListener.transportClosed();
        logger.debug("Closing WebSocket for session {}", getSession());
    }

    @Override
    public void onWebSocketConnect(Session sess)
    {
        connectionListener = wampRouter.connectionAcceptor().createNewConnectionListener();
        wampRouter.connectionAcceptor().acceptNewConnection(this, connectionListener);
        logger.debug("Opened WebSocket for session {}", sess);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        connectionListener.transportError(cause);
        logger.debug("WebSocket error for session {}", getSession(), cause);
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
        if (getSession().isOpen()) {
            try {
                getRemote().sendString(message.toObjectArray(objectMapper).toString(),
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
            }catch (WampError e) {
                promise.reject(e);
            }
        }else{
            promise.reject(new IllegalStateException("Connection is not open"));
        }
    }

    @Override
    public void close(boolean sendRemaining, IWampConnectionPromise<Void> promise)
    {
        try {
            if (sendRemaining) {
                getRemote().flush();
            }
            getSession().close();

            promise.fulfill(null);
        }catch (IOException e) {
            promise.reject(e);
        }
    }
}
