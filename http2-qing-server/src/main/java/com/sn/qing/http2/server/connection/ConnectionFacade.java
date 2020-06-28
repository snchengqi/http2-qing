package com.sn.qing.http2.server.connection;

import com.sn.qing.http2.core.entity.HttpPushAck;
import com.sn.qing.http2.core.entity.HttpPushEntity;
import com.sn.qing.http2.core.entity.HttpResponse;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Facade of http2 connection for server endpoint,
 * it provider written and closable methods and hide others
 * @author ChengQi
 * @date 2020-06-24 14:22
 */
public interface ConnectionFacade extends Closeable {

    /**
     * used to write http2 response message to client endpoint
     * @param response http2 response
     * @param streamId stream id
     * @return CompletableFuture
     */
    CompletableFuture<Void> sendResponse(HttpResponse response, int streamId);

    /**
     * used to write server-push message to client endpoint
     * @param pushEntity server-push message
     * @return ack
     */
    CompletableFuture<HttpPushAck> sendPushMessage(HttpPushEntity pushEntity);
}
