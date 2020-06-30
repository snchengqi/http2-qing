package com.sn.qing.http2.core.invoker;

import com.sn.qing.http2.core.entity.StreamMessage;
import io.netty.handler.codec.http2.Http2Stream;

/**
 * Top level abstract interface for {@link Http2Stream} reading,
 * hide http2 frame reading and provider stream message
 * @author ChengQi
 * @date 2020-06-18
 */
public interface StreamReaderInvoker {

    /**
     * it will be called after a integral message has been read
     * by {@link io.netty.handler.codec.http2.Http2FrameListener}
     * @param stream http2Stream
     * @param streamMessage integral message
     */
    void onStreamMessageRead(Http2Stream stream, StreamMessage streamMessage);

    /**
     * it will be called when happen error in the stream
     * @param stream http2Stream
     * @param cause integral message
     */
    void onStreamError(Http2Stream stream, Throwable cause);
}
