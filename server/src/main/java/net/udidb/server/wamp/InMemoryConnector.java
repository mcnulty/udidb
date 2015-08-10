/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.wamp;

import java.util.concurrent.ScheduledExecutorService;

import ws.wamp.jawampa.WampRouter;
import ws.wamp.jawampa.connection.IPendingWampConnection;
import ws.wamp.jawampa.connection.IPendingWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnector;

/**
 * IWampConnector that establishes a connection between a router and client by connecting IWampConnectionListeners to
 * the appropriate message source.
 *
 * @author mcnulty
 */
public class InMemoryConnector implements IWampConnector
{
    private final WampRouter wampRouter;

    public InMemoryConnector(WampRouter wampRouter)
    {
        this.wampRouter = wampRouter;
    }

    @Override
    public IPendingWampConnection connect(ScheduledExecutorService scheduler, IPendingWampConnectionListener connectListener, IWampConnectionListener clientConnectionListener)
    {
        IWampConnectionListener routerConnectionListener = wampRouter.connectionAcceptor().createNewConnectionListener();

        InMemoryConnection routerConnection = new InMemoryConnection(clientConnectionListener);
        wampRouter.connectionAcceptor().acceptNewConnection(routerConnection, routerConnectionListener);

        InMemoryConnection clientConnection = new InMemoryConnection(routerConnectionListener);
        connectListener.connectSucceeded(clientConnection);

        return () -> {
            // This should never occur because the connection is immediately established
            connectListener.connectFailed(new IllegalStateException());
        };
    }
}
