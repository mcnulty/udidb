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
    private static final long IDLE_TIMEOUT = 60 * 60 * 1000; // 1 hour

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
        webSocketServletFactory.getPolicy().setIdleTimeout(IDLE_TIMEOUT);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp)
    {
        for (String subprotocol : req.getSubProtocols()) {
            // Only allow the wamp.2.json protocol, if specified
            if (!subprotocol.equals("wamp.2.json")) {
                return null;
            }else{
                resp.setAcceptedSubProtocol("wamp.2.json");
            }
        }

        return injector.getInstance(EventsSocket.class);
    }
}
