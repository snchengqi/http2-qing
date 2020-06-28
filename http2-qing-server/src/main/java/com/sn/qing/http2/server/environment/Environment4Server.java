package com.sn.qing.http2.server.environment;

import com.sn.qing.http2.core.Environment;
import com.sn.qing.http2.server.connection.manager.ConnectionManager;
import com.sn.qing.http2.server.request.RequestHandler;

/**
 * @author ChengQi
 * @date 2020-06-24 16:04
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
