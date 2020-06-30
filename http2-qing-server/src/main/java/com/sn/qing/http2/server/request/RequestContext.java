package com.sn.qing.http2.server.request;

import com.sn.qing.http2.core.entity.HttpRequest;
import com.sn.qing.http2.server.connection.ConnectionFacade;
import io.netty.handler.codec.http2.Http2Stream;

import java.util.Objects;

/**
 * The context of client request, it aggregates request data or cause,
 * http2 stream and facade of connection which is used to send response for this request
 * @author ChengQi
 * @date 2020-06-24
 */
public class RequestContext {

    private ConnectionFacade connection;
    private Http2Stream stream;
    private HttpRequest request;
    private Throwable cause;

    private RequestContext(ConnectionFacade connection, Http2Stream stream,
                           HttpRequest request, Throwable cause) {
        this.connection = connection;
        this.stream = stream;
        this.request = request;
        this.cause = cause;
    }

    public static RequestContext create(ConnectionFacade connection, Http2Stream stream,
                                        HttpRequest request, Throwable cause) {
        return new RequestContext(connection, stream, request, cause);
    }

    public boolean isSuccess() {
        return Objects.isNull(cause);
    }

    public ConnectionFacade connection() {
        return connection;
    }

    public Http2Stream stream() {
        return stream;
    }

    public HttpRequest request() {
        return request;
    }

    public Throwable cause() {
        return cause;
    }
}
