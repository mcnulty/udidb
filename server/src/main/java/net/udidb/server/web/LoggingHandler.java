/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.web;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RoutingContextDecorator;

/**
 * Handler that logs the request and response
 */
public class LoggingHandler implements Handler<RoutingContext>
{
    private static final Logger logger = LoggerFactory.getLogger(LoggingHandler.class);

    private final Handler<RoutingContext> wrapped;
    private ResponseBodyCollector collector = null;

    public LoggingHandler(Handler<RoutingContext> wrapped)
    {
        this.wrapped = wrapped;
    }

    private void logRequest(RoutingContext context)
    {
        HttpServerRequest request = context.request();
        StringBuilder builder = new StringBuilder();
        builder.append("> ").append(request.method()).append(" ").append(request.uri()).append(System.lineSeparator());
        request.headers()
               .entries()
               .stream()
               .map(entry -> entry.getKey() + ": " + entry.getValue())
               .forEach(value -> builder.append("> ").append(value).append(System.lineSeparator()));
        builder.append("> ").append(System.lineSeparator());
        String body = context.getBodyAsString();
        if (body != null && !body.isEmpty()) {
            builder.append("> ").append(body);
        }
        logger.debug("{}{}", System.lineSeparator(), builder.toString());
    }

    private void logResponse(RoutingContext context)
    {
        HttpServerResponse response = context.response();
        StringBuilder builder = new StringBuilder();
        builder.append("< ").append(response.getStatusCode()).append(" ").append(response.getStatusMessage()).append(System.lineSeparator());
        response.headers()
                .entries()
                .stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .forEach(value -> builder.append("< ").append(value).append(System.lineSeparator()));
        builder.append("< ").append(System.lineSeparator());

        if (collector != null) {
            String bodyData = collector.getBodyData();
            if (!bodyData.isEmpty()) {
                builder.append("< ").append(bodyData);
            }
        }
        logger.debug("{}{}", System.lineSeparator(), builder.toString());
    }

    @Override
    public void handle(RoutingContext context)
    {
        logRequest(context);
        context.addBodyEndHandler((unused) -> logResponse(context));

        collector = new ResponseBodyCollector(context.currentRoute(), context);
        wrapped.handle(collector);
    }

    public static boolean isEnabled()
    {
        return logger.isDebugEnabled();
    }

    private static class ResponseBodyCollector extends RoutingContextDecorator implements HttpServerResponse
    {
        private final Queue<String> bodyData = new LinkedList<>();

        public ResponseBodyCollector(Route currentRoute, RoutingContext decoratedContext)
        {
            super(currentRoute, decoratedContext);
        }

        public String getBodyData()
        {
            return bodyData.stream().collect(Collectors.joining());
        }

        @Override
        public HttpServerResponse response()
        {
            return this;
        }

        @Override
        public HttpServerResponse write(Buffer data)
        {
            bodyData.add(data.toString());
            super.response().write(data);
            return this;
        }

        @Override
        public void end(Buffer chunk)
        {
            bodyData.add(chunk.toString());
            super.response().end(chunk);
        }

        @Override
        public HttpServerResponse write(String chunk, String enc)
        {
            bodyData.add(chunk);
            super.response().write(chunk, enc);
            return this;
        }

        @Override
        public HttpServerResponse write(String chunk)
        {
            bodyData.add(chunk);
            super.response().write(chunk);
            return this;
        }

        @Override
        public void end(String chunk)
        {
            bodyData.add(chunk);
            super.response().end(chunk);
        }

        @Override
        public void end(String chunk, String enc)
        {
            bodyData.add(chunk);
            super.response().end(chunk, enc);
        }

        @Override
        public void end()
        {
            super.response().end();
        }

        @Override
        public HttpServerResponse exceptionHandler(Handler<Throwable> handler)
        {
            super.response().exceptionHandler(handler);
            return this;
        }

        @Override
        public HttpServerResponse setWriteQueueMaxSize(int maxSize)
        {
            super.response().setWriteQueueMaxSize(maxSize);
            return this;
        }

        @Override
        public boolean writeQueueFull()
        {
            return super.response().writeQueueFull();
        }

        @Override
        public HttpServerResponse drainHandler(Handler<Void> handler)
        {
            super.response().drainHandler(handler);
            return this;
        }

        @Override
        public int getStatusCode()
        {
            return super.response().getStatusCode();
        }

        @Override
        public HttpServerResponse setStatusCode(int statusCode)
        {
            super.response().setStatusCode(statusCode);
            return this;
        }

        @Override
        public String getStatusMessage()
        {
            return super.response().getStatusMessage();
        }

        @Override
        public HttpServerResponse setStatusMessage(String statusMessage)
        {
            super.response().setStatusMessage(statusMessage);
            return this;
        }

        @Override
        public HttpServerResponse setChunked(boolean chunked)
        {
            super.response().setChunked(chunked);
            return this;
        }

        @Override
        public boolean isChunked()
        {
            return super.response().isChunked();
        }

        @Override
        public MultiMap headers()
        {
            return super.response().headers();
        }

        @Override
        public HttpServerResponse putHeader(String name, String value)
        {
            super.response().putHeader(name, value);
            return this;
        }

        @Override
        public HttpServerResponse putHeader(CharSequence name, CharSequence value)
        {
            super.response().putHeader(name, value);
            return this;
        }

        @Override
        public HttpServerResponse putHeader(String name, Iterable<String> values)
        {
            super.response().putHeader(name, values);
            return this;
        }

        @Override
        public HttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values)
        {
            super.response().putHeader(name, values);
            return this;
        }

        @Override
        public MultiMap trailers()
        {
            return super.response().trailers();
        }

        @Override
        public HttpServerResponse putTrailer(String name, String value)
        {
            super.response().putTrailer(name, value);
            return this;
        }

        @Override
        public HttpServerResponse putTrailer(CharSequence name, CharSequence value)
        {
            super.response().putTrailer(name, value);
            return this;
        }

        @Override
        public HttpServerResponse putTrailer(String name, Iterable<String> values)
        {
            super.response().putTrailer(name, values);
            return this;
        }

        @Override
        public HttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> value)
        {
            super.response().putTrailer(name, value);
            return this;
        }

        @Override
        public HttpServerResponse closeHandler(Handler<Void> handler)
        {
            super.response().closeHandler(handler);
            return this;
        }

        @Override
        public HttpServerResponse endHandler(Handler<Void> handler)
        {
            super.response().endHandler(handler);
            return this;
        }

        @Override
        public HttpServerResponse writeContinue()
        {
            super.response().writeContinue();
            return this;
        }

        @Override
        public HttpServerResponse sendFile(String filename, long offset, long length)
        {
            super.response().sendFile(filename, offset, length);
            return this;
        }

        @Override
        public HttpServerResponse sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> resultHandler)
        {
            super.response().sendFile(filename, offset, length, resultHandler);
            return this;
        }

        @Override
        public void close()
        {
            super.response().close();
        }

        @Override
        public boolean ended()
        {
            return super.response().ended();
        }

        @Override
        public boolean closed()
        {
            return super.response().closed();
        }

        @Override
        public boolean headWritten()
        {
            return super.response().headWritten();
        }

        @Override
        public HttpServerResponse headersEndHandler(Handler<Void> handler)
        {
            super.response().headersEndHandler(handler);
            return this;
        }

        @Override
        public HttpServerResponse bodyEndHandler(Handler<Void> handler)
        {
            super.response().bodyEndHandler(handler);
            return this;
        }

        @Override
        public long bytesWritten()
        {
            return super.response().bytesWritten();
        }

        @Override
        public int streamId()
        {
            return 0;
        }

        @Override
        public HttpServerResponse push(HttpMethod method, String host, String path, Handler<AsyncResult<HttpServerResponse>> handler)
        {
            super.response().push(method, host, path, handler);
            return this;
        }

        @Override
        public HttpServerResponse push(HttpMethod method, String path, MultiMap headers, Handler<AsyncResult<HttpServerResponse>> handler)
        {
            super.response().push(method, path, headers, handler);
            return this;
        }

        @Override
        public HttpServerResponse push(HttpMethod method, String path, Handler<AsyncResult<HttpServerResponse>> handler)
        {
            super.response().push(method, path, handler);
            return this;
        }

        @Override
        public HttpServerResponse push(HttpMethod method,
                                       String host,
                                       String path,
                                       MultiMap headers,
                                       Handler<AsyncResult<HttpServerResponse>> handler)
        {
            super.response().push(method, host, path, headers, handler);
            return this;
        }

        @Override
        public void reset(long code)
        {
            super.response().reset(code);
        }

        @Override
        public HttpServerResponse writeCustomFrame(int type, int flags, Buffer payload)
        {
            super.response().writeCustomFrame(type, flags, payload);
            return this;
        }
    }
}
