/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.wamp;

import ws.wamp.jawampa.WampMessages.WampMessage;
import ws.wamp.jawampa.WampSerialization;
import ws.wamp.jawampa.connection.IWampConnection;
import ws.wamp.jawampa.connection.IWampConnectionListener;
import ws.wamp.jawampa.connection.IWampConnectionPromise;

/**
 * A IWampConnection that routes sent messages directly to a IWampConnectionListener
 *
 * @author mcnulty
 */
public class InMemoryConnection implements IWampConnection
{
    private final IWampConnectionListener connectionListener;

    public InMemoryConnection(IWampConnectionListener connectionListener)
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
        connectionListener.messageReceived(message);
        promise.fulfill(null);
    }

    @Override
    public void close(boolean sendRemaining, IWampConnectionPromise<Void> promise)
    {
        connectionListener.transportClosed();
        promise.fulfill(null);
    }
}
