package com.sn.qing.http2.client.reconnect;

import com.sn.qing.http2.client.DefaultHttp2Client;

/**
 * when lost connect, the client will use the functional interface to do reconnect
 * @see DefaultHttp2Client
 * @author ChengQi
 * @date 2020-06-23
 */
@FunctionalInterface
public interface ReconnectPolicy {

    /**
     * whether or support reconnect to server, default support
     * @return {@code true} if support, {@code false} if not
     */
    default boolean supportReconnect() {
        return true;
    }

    /**
     * compute next connect times after previous disconnect
     * @param connectTimes current connect to server times
     * @return reconnect interval time, unit mills
     * @throws Exception if throw any exception, the client won`t do connect
     */
    int nextReconnectInterval(int connectTimes) throws Exception;
}
