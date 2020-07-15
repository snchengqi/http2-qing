package com.github.snchengqi.qing.http2.client.environment;

import com.github.snchengqi.qing.http2.client.Http2Client;
import com.github.snchengqi.qing.http2.client.push.ServerPushHandler;
import com.github.snchengqi.qing.http2.client.reconnect.ReconnectPolicy;
import com.github.snchengqi.qing.http2.core.Environment;

/**
 * The sub class of {@link Environment}
 * for constructing {@link Http2Client}
 * @author ChengQi
 * @date 2020-06-24
 */
public class Environment4Client extends Environment<Environment4Client> {

    private boolean pushEnable = true;

    private int connectTimeoutMills = 5000;

    private ReconnectPolicy reconnectPolicy;

    private ServerPushHandler serverPushHandler;

    @Override
    protected Environment4Client self() {
        return this;
    }

    public boolean isPushEnable() {
        return pushEnable;
    }

    public Environment4Client setPushEnable(boolean pushEnable) {
        this.pushEnable = pushEnable;
        return self();
    }

    public int getConnectTimeoutMills() {
        return connectTimeoutMills;
    }

    public Environment4Client setConnectTimeoutMills(int connectTimeoutMills) {
        this.connectTimeoutMills = connectTimeoutMills;
        return self();
    }

    public ReconnectPolicy getReconnectPolicy() {
        return reconnectPolicy;
    }

    public Environment4Client setReconnectPolicy(ReconnectPolicy reconnectPolicy) {
        this.reconnectPolicy = reconnectPolicy;
        return self();
    }

    public ServerPushHandler getServerPushHandler() {
        return serverPushHandler;
    }

    public Environment4Client setServerPushHandler(ServerPushHandler serverPushHandler) {
        this.serverPushHandler = serverPushHandler;
        return self();
    }
}
