package com.github.snchengqi.qing.http2.server.environment;

import com.github.snchengqi.qing.http2.core.Environment;
import com.github.snchengqi.qing.http2.server.Http2Server;
import com.github.snchengqi.qing.http2.server.connection.manager.ConnectionManager;
import com.github.snchengqi.qing.http2.server.request.RequestHandler;

/**
 * The sub class of {@link Environment} for constructing
 * {@link Http2Server}
 * @author ChengQi
 * @date 2020-06-24
 */
public class Environment4Server extends Environment<Environment4Server> {

    private String privateKeyPath;
    private RequestHandler requestHandler;
    private ConnectionManager connectionManager;

    @Override
    protected Environment4Server self() {
        return this;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public Environment4Server setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        return self();
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public Environment4Server setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        return self();
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public Environment4Server setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        return self();
    }
}
