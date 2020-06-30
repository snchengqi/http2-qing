package com.sn.qing.http2.core;

import com.sn.qing.http2.core.connection.Connection;
import com.sn.qing.http2.core.entity.StreamMessage;
import io.netty.handler.codec.http2.Http2Stream;

/**
 * Callback of stream message when reading
 * @author ChengQi
 * @date 2020-06-19
 */
public interface StreamReaderListener {

    /**
     * called after a integral message read
     * @param connection the connection for http2
     * @param stream http2Stream
     * @param streamMessage a integral message
     */
    void onStreamMessageRead(Connection connection, Http2Stream stream, StreamMessage streamMessage);

    /**
     * called after happen exception
     * @param connection the connection for http2
     * @param stream http2Stream
     * @param cause throwable
     */
    void onStreamError(Connection connection, Http2Stream stream, Throwable cause);
}
