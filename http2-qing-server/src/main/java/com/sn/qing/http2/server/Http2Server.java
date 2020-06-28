package com.sn.qing.http2.server;

import com.sn.qing.http2.server.connection.manager.ConnectionManager;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Interface of http2 server, it provider methods to start and shutdown server,
 * also will manage client connections using {@link ConnectionManager}
 * @author ChengQi
 * @date 2020-06-24 13:53
 */
public interface Http2Server extends Closeable {

    /**
     * start server and bind in port which designated
     * @return CompletableFuture contains connectionManager
     */
    CompletableFuture<Void> start();

    /**
     * get current count of connection
     * @return connection count
     */
    int connectionCount();
}
