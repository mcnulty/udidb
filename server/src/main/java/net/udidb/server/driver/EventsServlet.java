/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * @author mcnulty
 */
@Singleton
public class EventsServlet extends WebSocketServlet implements WebSocketCreator
{
    private final Injector injector;

    @Inject
    public EventsServlet(Injector injector)
    {
        this.injector = injector;
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory)
    {
        webSocketServletFactory.setCreator(this);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
    {
        return injector.getInstance(EventsSocket.class);
    }
}
