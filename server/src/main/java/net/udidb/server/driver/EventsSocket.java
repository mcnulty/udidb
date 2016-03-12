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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
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
public class EventsSocket implements IWampConnection
{
    private static final Logger logger = LoggerFactory.getLogger(EventsSocket.class);

    private final WampRouter wampRouter;
    private final ObjectMapper objectMapper;

    private ServerWebSocket serverWebSocket;
    private IWampConnectionListener connectionListener;

    @Inject
    public EventsSocket(WampRouter wampRouter, ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
        this.wampRouter = wampRouter;
    }

    public void setServerWebSocket(ServerWebSocket serverWebSocket)
    {
        this.serverWebSocket = serverWebSocket;
        this.serverWebSocket.frameHandler(frameHandler());
        this.serverWebSocket.exceptionHandler(exceptionHandler());
        this.serverWebSocket.closeHandler(closeHandler());

        connectionListener = wampRouter.connectionAcceptor().createNewConnectionListener();
        wampRouter.connectionAcceptor().acceptNewConnection(this, connectionListener);
        logger.debug("Opened WebSocket[{} <=> {}]",
                serverWebSocket.remoteAddress(),
                serverWebSocket.localAddress());

    }

    private Handler<WebSocketFrame> frameHandler()
    {
        return frame -> {
            if (frame.isBinary()) {
                throw new IllegalArgumentException("Binary messages are not supported by this server");
            }

            // TODO this doesn't handle multi-frame messages
            String message = frame.textData();

            logger.debug("[SERVER] Received WAMP message {}", message);
            passMessageToWampListener(message, objectMapper, connectionListener);
        };
    }

    private Handler<Throwable> exceptionHandler()
    {
        return cause -> {
            connectionListener.transportError(cause);
            logger.debug("Error for WebSocket[{} <=> {}]",
                    serverWebSocket.remoteAddress(),
                    serverWebSocket.localAddress(),
                    cause);
        };
    }

    private Handler<Void> closeHandler()
    {
        return (v) -> {
            connectionListener.transportClosed();
            logger.debug("Closing WebSocket[{} <=> {}]",
                    serverWebSocket.remoteAddress(),
                    serverWebSocket.localAddress());
        };
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
            if (serverWebSocket != null) {
                try {
                    String rawMessage = objectMapper.writeValueAsString(message.toObjectArray(objectMapper));
                    logger.debug("[SERVER] Sending WAMP message {}", rawMessage);
                    serverWebSocket.writeFinalTextFrame(rawMessage);
                    promise.fulfill(null);
                } catch (IOException | WampError e) {
                    logger.error("Recieved exception while sending message", e);
                    promise.reject(e);
                }
            } else {
                promise.reject(new IllegalStateException("Connection is not open"));
            }
        }
        catch (RuntimeException e)
        {
            logger.error("Received exception while sending message", e);
            promise.reject(e);
        }
    }

    @Override
    public void close(boolean sendRemaining, IWampConnectionPromise<Void> promise)
    {
        serverWebSocket.close();
        promise.fulfill(null);
    }
}
