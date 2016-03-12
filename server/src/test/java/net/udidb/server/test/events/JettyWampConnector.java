/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test.events;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.client.io.UpgradeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.wamp.jawampa.connection.IPendingWampConnection;
import ws.wamp.jawampa.connection.IPendingWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnector;

/**
 * @author mcnulty
 */
public class JettyWampConnector implements IWampConnector
{
    private static final Logger logger = LoggerFactory.getLogger(JettyWampConnector.class);

    private final URI uri;

    public JettyWampConnector(URI uri)
    {
        this.uri = uri;
    }

    @Override
    public IPendingWampConnection connect(ScheduledExecutorService scheduler, IPendingWampConnectionListener connectListener, IWampConnectionListener connectionListener)
    {
        WebSocketClient client = new WebSocketClient();
        try {
            client.start();

            JettyWampConnection connection = new JettyWampConnection(connectListener, connectionListener);

            ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
            upgradeRequest.setRequestURI(uri);
            client.connect(connection, uri, upgradeRequest, new UpgradeListener()
            {

                @Override
                public void onHandshakeRequest(UpgradeRequest request)
                {
                    // Ignored
                }

                @Override
                public void onHandshakeResponse(UpgradeResponse response)
                {
                    if (!response.isSuccess()) {
                        connectListener.connectFailed(new Exception("Failed to open websocket: " + response.getStatusCode() + " " + response.getStatusReason()));
                    }
                    // If the handshake was successful, wait to complete the WAMP connection until the session is passed
                    // to the JettyWampConnection
                }
            });
        }catch (Exception e) {
            connectListener.connectFailed(e);
        }

        return () -> {
            if (!client.isStopped()) {
                try {
                    client.stop();
                }catch (Exception e) {
                    logger.error("Failed to cancel connection", e);
                }
            }
        };
    }
}
