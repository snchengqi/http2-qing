package com.github.snchengqi.qing.http2.client;

import com.github.snchengqi.qing.http2.core.entity.HttpPushAck;
import com.github.snchengqi.qing.http2.core.entity.HttpRequest;
import com.github.snchengqi.qing.http2.core.entity.HttpResponse;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * Http2Client interface, it defines ability for connect to remote,
 * send message asynchronously and so on
 * @author ChengQi
 * @date 2020-06-22
 */
public interface Http2Client extends Closeable {

    /**
     * connect to remote address asynchronously
     * @return CompletableFuture contains {@link Void}
     */
    CompletableFuture<Void> connect();

    /**
     * send http request to remote address asynchronously
     * @param request http2 request
     * @return CompletableFuture contains response
     */
    CompletableFuture<HttpResponse> sendRequest(HttpRequest request);

    /**
     * send http request to remote address asynchronously,
     * and not wait far response
     * @param request http2 request
     * @return CompletableFuture contains nothing
     */
    CompletableFuture<Void> sendRequestOneWay(HttpRequest request);

    /**
     * send server-push ack to remote address asynchronously
     * @param pushAck server-push ack
     * @param streamId http2 streamId
     * @return CompletableFuture contains nothing
     */
    CompletableFuture<Void> sendPushAck(HttpPushAck pushAck, int streamId);
}
