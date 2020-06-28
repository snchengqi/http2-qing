package com.sn.qing.http2.core.entity;

import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author ChengQi
 * @date 2020-06-22 17:13
 */
public class HttpEntity {

    private Http2Headers headers;
    private byte[] body;

    public HttpEntity() {}

    public HttpEntity(StreamMessage streamMessage) {
        this.headers = streamMessage.getHeaders();
        this.body = streamMessage.getData();
    }

    public HttpEntity(Http2Headers headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    public StreamMessage toStreamMessage() {
        return new StreamMessage(headers, body);
    }

    public Http2Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Http2Headers headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
