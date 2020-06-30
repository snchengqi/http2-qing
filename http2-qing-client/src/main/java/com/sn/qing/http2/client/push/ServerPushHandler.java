package com.sn.qing.http2.client.push;

import com.sn.qing.http2.client.Http2Client;

/**
 * The functional interface is use to handle server-push
 * @author ChengQi
 * @date 2020-06-23
 */
@FunctionalInterface
public interface ServerPushHandler {

    /**
     * the method will be called when {@link Http2Client}
     * receive push message from server
     * @param context contains client, connection, stream, message or cause
     */
    void onReceived(ServerPushContext context);
}
