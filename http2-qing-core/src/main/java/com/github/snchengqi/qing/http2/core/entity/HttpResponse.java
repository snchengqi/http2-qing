package com.github.snchengqi.qing.http2.core.entity;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Headers;

/**
 * @author ChengQi
 * @date 2020-06-22
 */
public class HttpResponse extends HttpEntity {

    public HttpResponse(StreamMessage streamMessage) {
        super(streamMessage);
    }

    public HttpResponse(Http2Headers headers, byte[] body) {
        super(headers, body);
    }

    public HttpResponseStatus status() {
        return HttpResponseStatus.parseLine(getHeaders().status());
    }
}
