package com.sn.qing.http2.core.invoker;

import com.sn.qing.http2.core.StreamReaderListener;
import com.sn.qing.http2.core.entity.StreamMessage;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Stream;

import java.util.concurrent.CompletableFuture;

/**
 * @author ChengQi
 * @date 2020-06-18
 */
public interface StreamWriterInvoker {

    /**
     * write a integral message to remote endpoint
     * @param streamMessage integral message
     * @return CompletableFuture contains StreamMessage
     */
    CompletableFuture<StreamMessage> writeStreamMessage(StreamMessage streamMessage);

    /**
     * write a integral message to remote endpoint
     * @param streamMessage integral message
     * @param listener the listener to process while reading integral message or throw a exception
     * @return CompletableFuture contains nothing
     */
    CompletableFuture<Void> writeStreamMessage(StreamMessage streamMessage, StreamReaderListener listener);

    /**
     * write a integral message back to remote endpoint
     * @param streamMessage integral message
     * @param streamId stream id which write back
     * @return CompletableFuture contains nothing
     */
    CompletableFuture<Void> writeStreamMessage(StreamMessage streamMessage, int streamId);

    /**
     * write http2 headers to remote endpoint
     * @param headers http2Headers
     * @param endStream end of stream
     * @param listener StreamReaderListener
     * @return future
     */
    CompletableFuture<Http2Stream> writeHeaders(Http2Headers headers, boolean endStream, StreamReaderListener listener);

    /**
     * write http2 headers back to remote endpoint
     * @param headers http2Headers
     * @param endStream end of stream
     * @param streamId stream id which write back
     * @return future
     */
    CompletableFuture<Http2Stream> writeHeaders(Http2Headers headers, boolean endStream, int streamId);

    /**
     * write http2 data to remote endpoint
     * @param stream stream which to write
     * @param data data waiting to write
     * @param endStream end of stream
     * @return future
     */
    CompletableFuture<Http2Stream> writeData(Http2Stream stream, byte[] data, boolean endStream);
}
