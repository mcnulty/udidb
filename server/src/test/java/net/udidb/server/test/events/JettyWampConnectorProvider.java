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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ws.wamp.jawampa.WampSerialization;
import ws.wamp.jawampa.connection.IWampClientConnectionConfig;
import ws.wamp.jawampa.connection.IWampConnector;
import ws.wamp.jawampa.connection.IWampConnectorProvider;

/**
 * @author mcnulty
 */
public class JettyWampConnectorProvider implements IWampConnectorProvider
{

    @Override
    public ScheduledExecutorService createScheduler()
    {
        return Executors.newScheduledThreadPool(1);
    }

    @Override
    public IWampConnector createConnector(URI uri, IWampClientConnectionConfig configuration, List<WampSerialization> serializations)
    {
        return new JettyWampConnector(uri);
    }
}
