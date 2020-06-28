package com.sn.qing.http2.core.entity;

import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author ChengQi
 * @date 2020-06-23 17:12
 */
public class HttpPushAck extends HttpResponse {

    public HttpPushAck(StreamMessage streamMessage) {
        super(streamMessage);
    }

    public HttpPushAck(Http2Headers headers, byte[] body) {
        super(headers, body);
    }
}
