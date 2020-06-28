package com.sn.qing.http2.core.entity;

import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author ChengQi
 * @date 2020-06-22 17:16
 */
public class HttpRequest extends HttpEntity {

    public HttpRequest(StreamMessage streamMessage) {
        super(streamMessage);
    }

    public HttpRequest(Http2Headers headers, byte[] body) {
        super(headers, body);
    }
}
