package com.sn.qing.http2.client.push;

import com.sn.qing.http2.client.Http2Client;
import com.sn.qing.http2.core.entity.HttpPushEntity;
import io.netty.handler.codec.http2.Http2Stream;

import java.util.Objects;

/**
 * @author ChengQi
 * @date 2020-06-23 17:29
 */
public class ServerPushContext {

    private Http2Client client;
    private Http2Stream stream;
    private HttpPushEntity pushEntity;
    private Throwable cause;

    private ServerPushContext(Http2Client client, Http2Stream stream,
                              HttpPushEntity pushEntity, Throwable cause) {
        this.client = client;
        this.stream = stream;
        this.pushEntity = pushEntity;
        this.cause = cause;
    }

    public static ServerPushContext of(Http2Client client, Http2Stream stream,
                                       HttpPushEntity pushEntity, Throwable cause) {
        return new ServerPushContext(client, stream, pushEntity, cause);
    }

    public boolean isSuccess() {
        return Objects.isNull(cause);
    }

    public Http2Client client() {
        return client;
    }

    public Http2Stream stream() {
        return stream;
    }

    public HttpPushEntity pushEntity() {
        return pushEntity;
    }

    public Throwable cause() {
        return cause;
    }
}
