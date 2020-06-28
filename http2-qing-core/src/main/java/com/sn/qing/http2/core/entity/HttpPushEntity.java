package com.sn.qing.http2.core.entity;

import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author ChengQi
 * @date 2020-06-23 17:11
 */
public class HttpPushEntity extends HttpEntity {

    public HttpPushEntity(StreamMessage streamMessage) {
        super(streamMessage);
    }

    public HttpPushEntity(Http2Headers headers, byte[] body) {
        super(headers, body);
    }
}
