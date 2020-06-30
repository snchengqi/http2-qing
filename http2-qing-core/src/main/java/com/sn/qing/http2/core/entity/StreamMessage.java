package com.sn.qing.http2.core.entity;

import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author ChengQi
 * @date 2020-06-18
 */
public class StreamMessage {

    private Http2Headers headers;
    private byte[] data;

    public StreamMessage() {}

    public StreamMessage(Http2Headers headers) {
        this.headers = headers;
    }

    public StreamMessage(Http2Headers headers, byte[] data) {
        this.headers = headers;
        this.data = data;
    }

    public Http2Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Http2Headers headers) {
        this.headers = headers;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
