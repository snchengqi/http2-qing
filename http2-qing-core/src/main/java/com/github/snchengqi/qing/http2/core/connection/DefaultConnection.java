package com.github.snchengqi.qing.http2.core.connection;

import com.github.snchengqi.qing.http2.core.ChannelHandlerContextAware;
import com.github.snchengqi.qing.http2.core.StreamReaderListener;
import com.github.snchengqi.qing.http2.core.entity.StreamMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http2.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Default implement of {@link Connection} based on netty-http2
 * @author ChengQi
 * @date 2020-06-18
 */
public class DefaultConnection implements Connection, ChannelHandlerContextAware {

    private final Http2Connection connection;
    private final Http2ConnectionEncoder encoder;
    private final Http2ConnectionDecoder decoder;
    private final Http2Connection.PropertyKey streamMessageKey;
    private final Http2Connection.PropertyKey streamListenerKey;

    private final Set<Listener> listeners;

    private StreamReaderListener defaultStreamListener;

    private ChannelHandlerContext context;
    private State state;

    /**
     * this is a empty implement class of {@link StreamReaderListener},
     * for send unidirectional message
     */
    private StreamReaderListener doNothingListener = new StreamReaderListener() {
        @Override
        public void onStreamMessageRead(Connection connection, Http2Stream stream, StreamMessage streamMessage) {

        }

        @Override
        public void onStreamError(Connection connection, Http2Stream stream, Throwable cause) {

        }
    };

    /**
     * {@link Http2FrameListener} instance which process header and data frame,
     * forward to {@code #onStreamMessage}
     */
    private final Http2FrameListener innerListener = new Http2FrameAdapter() {

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
                throws Http2Exception {
            Http2Stream stream = connection.stream(streamId);
            int readableSize = data.readableBytes();
            byte[] bytes;
            if (padding == 0 && data.hasArray()) {
                bytes = data.array();
            } else {
                int dataSize = readableSize - padding;
                byte[] temp = new byte[readableSize];
                data.readBytes(temp, 0, readableSize);
                if (padding == 0) {
                    bytes = temp;
                } else {
                    bytes = Arrays.copyOf(temp, dataSize);
                }
            }
            StreamMessage streamMessage = stream.getProperty(streamMessageKey);
            if (Objects.isNull(streamMessage)) {
                Http2Exception ex = Http2Exception.streamError(streamId,
                        Http2Error.INTERNAL_ERROR, "Http2 headers not read");
                onStreamError(stream, ex);
                throw ex;
            }
            byte[] dataBytes = streamMessage.getData();
            if (Objects.isNull(dataBytes)) {
                streamMessage.setData(bytes);
            } else {
                int length = dataBytes.length + bytes.length;
                byte[] newDataBytes = new byte[length];
                System.arraycopy(dataBytes, 0, newDataBytes, 0, dataBytes.length);
                System.arraycopy(bytes, 0, newDataBytes, dataBytes.length, bytes.length);
                streamMessage.setData(newDataBytes);
            }
            if (endOfStream) {
                stream.removeProperty(streamMessageKey);
                onStreamMessageRead(stream, streamMessage);
            }
            return readableSize;
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                                  boolean endStream) throws Http2Exception {
            Http2Stream stream = connection.stream(streamId);
            StreamMessage streamMessage = new StreamMessage(headers);
            stream.setProperty(streamMessageKey, streamMessage);
            if (endStream) {
                stream.removeProperty(streamMessageKey);
                onStreamMessageRead(stream, streamMessage);
            }
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                                  short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
            onHeadersRead(ctx, streamId, headers, padding, endStream);
        }

        @Override
        public void onSettingsAckRead(ChannelHandlerContext ctx) throws Http2Exception {
            state(State.CREATED);
        }

        @Override
        public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
            Http2Stream stream = connection.stream(streamId);
            applyStreamListener(stream, listener -> listener.onStreamError(DefaultConnection.this,
                    stream, new IOException("rst stream receive, error code:" + errorCode)));
        }

        @Override
        public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags,
                                   ByteBuf payload) {
            Http2Stream stream = connection.stream(streamId);
            applyStreamListener(stream, listener -> listener.onStreamError(DefaultConnection.this,
                    stream, new IOException("unknown frame receive, payload:" + ByteBufUtil.hexDump(payload))));
        }
    };

    public DefaultConnection(Http2ConnectionEncoder encoder, Http2ConnectionDecoder decoder) {
        this.connection = encoder.connection();
        this.encoder = encoder;
        this.decoder = decoder;
        this.streamMessageKey = connection.newKey();
        this.streamListenerKey = connection.newKey();
        this.listeners = new ConcurrentSkipListSet<>();
    }

    /**
     * get an instance which process header and data frame
     * @return innerHttp2FrameListener
     */
    public Http2FrameListener getFrameListener() {
        return innerListener;
    }

    /**
     * register the default {@link StreamReaderListener} for reveal all the details
     * @param defaultStreamListener StreamReaderListener
     */
    @Override
    public void setDefaultStreamListener(StreamReaderListener defaultStreamListener) {
        this.defaultStreamListener = defaultStreamListener;
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public void state(State state) {
        this.state = state;
        listeners.forEach(listener -> listener.onStateChange(this, state));
    }

    @Override
    public void onError(Throwable cause) {
        try {
            connection.forEachActiveStream(stream -> {
                applyStreamListener(stream, listener -> listener.onStreamError(this, stream, cause));
                return true;
            });
        } catch (Http2Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void onStreamMessageRead(Http2Stream stream, StreamMessage streamMessage) {
        applyStreamListener(stream, listener -> listener.onStreamMessageRead(this, stream, streamMessage));
    }

    @Override
    public void onStreamError(Http2Stream stream, Throwable cause) {
        applyStreamListener(stream, listener -> listener.onStreamError(this, stream, cause));
    }

    @Override
    public CompletableFuture<StreamMessage> writeStreamMessage(StreamMessage streamMessage) {
        CompletableFuture<StreamMessage> future = new CompletableFuture<>();
        writeStreamMessage(streamMessage, new StreamReaderListener() {
            @Override
            public void onStreamMessageRead(Connection connection, Http2Stream stream, StreamMessage streamMessage) {
                future.complete(streamMessage);
            }
            @Override
            public void onStreamError(Connection connection, Http2Stream stream, Throwable cause) {
                future.completeExceptionally(cause);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> writeStreamMessage(StreamMessage streamMessage, StreamReaderListener listener) {
        checkStreamMessage(streamMessage);
        CompletableFuture<Void> future = new CompletableFuture<>();
        Http2Headers headers = streamMessage.getHeaders();
        byte[] data = streamMessage.getData();
        boolean endStream = Objects.isNull(data) || data.length == 0;
        CompletableFuture<Http2Stream> headersFuture = writeHeaders(headers, endStream, listener);
        if (endStream) {
            headersFuture.whenComplete((r, cause) -> {
                if (Objects.isNull(cause)) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(cause);
                }
            });
            return future;
        }
        headersFuture.thenAccept(r -> writeData(r, data, true)).whenComplete((r, cause) -> {
           if (Objects.isNull(cause)) {
               future.complete(null);
           } else {
               future.completeExceptionally(cause);
           }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> writeStreamMessage(StreamMessage streamMessage, int streamId) {
        checkStreamMessage(streamMessage);
        CompletableFuture<Void> future = new CompletableFuture<>();
        Http2Headers headers = streamMessage.getHeaders();
        byte[] data = streamMessage.getData();
        boolean endStream = Objects.isNull(data) || data.length == 0;
        CompletableFuture<Http2Stream> headersFuture = writeHeaders(headers, endStream, streamId);
        if (endStream) {
            headersFuture.whenComplete((r, cause) -> {
                if (Objects.isNull(cause)) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(cause);
                }
            });
            return future;
        }
        headersFuture.thenAccept(r -> writeData(r, data, true)).whenComplete((r, cause) -> {
            if (Objects.isNull(cause)) {
                future.complete(null);
            } else {
                future.completeExceptionally(cause);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Http2Stream> writeHeaders(Http2Headers headers, boolean endStream, StreamReaderListener listener) {
        try {
            if (Objects.isNull(listener)) {
                listener = doNothingListener;
            }
            StreamReaderListener finalListener = listener;
            return doInEventLoop(() -> {
                try {
                    int streamId = connection.local().incrementAndGetNextStreamId();
                    return connection.local().createStream(streamId, false);
                } catch (Http2Exception e) {
                    throw new IllegalStateException(e);
                }
            }, (stream, promise) -> {
                stream.setProperty(streamListenerKey, finalListener);
                encoder.writeHeaders(context, stream.id(), headers, 0, endStream, promise);
                context.pipeline().flush();
            });
        } catch (Exception e) {
            CompletableFuture<Http2Stream> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Http2Stream> writeHeaders(Http2Headers headers, boolean endStream, int streamId) {
        Http2Stream stream = connection.stream(streamId);
        Objects.requireNonNull(stream, "stream not exist");
        return doInEventLoop(() -> stream, (stream_, promise) -> {
            encoder.writeHeaders(context, streamId, headers, 0, endStream, promise);
            context.pipeline().flush();
        });
    }

    @Override
    public CompletableFuture<Http2Stream> writeData(Http2Stream stream, byte[] data, boolean endStream) {
        return doInEventLoop(() -> stream, (stream_, promise) -> {
            ByteBuf byteBuf = context.alloc().buffer().writeBytes(data);
            encoder.writeData(context, stream.id(), byteBuf, 0, endStream, promise);
            context.pipeline().flush();
        });
    }

    @Override
    public void setChannelHandlerContext(ChannelHandlerContext context) {
        this.context = context;
        this.context.channel().closeFuture().addListener(future -> this.state(State.CLOSED));
    }

    /**
     * check streamMessage, both streamMessage and headers can not be null
     * @param streamMessage streamMessage
     */
    private void checkStreamMessage(StreamMessage streamMessage) {
        Objects.requireNonNull(streamMessage, "parameter streamMessage must not be null");
        Objects.requireNonNull(streamMessage.getHeaders(), "headers must not be null");
    }

    /**
     * execute consumer function in netty event loop thread
     * @param supplier result for future
     * @param consumer consumer function
     * @param <R> type of result
     * @return future
     */
    private <R> CompletableFuture<R> doInEventLoop(Supplier<R> supplier, BiConsumer<R, ChannelPromise> consumer) {
        CompletableFuture<R> cf = new CompletableFuture<>();

        EventLoop eventLoop = context.channel().eventLoop();
        if (eventLoop.inEventLoop()) {
            R result = supplier.get();
            ChannelPromise promise = context.newPromise().addListener(future -> {
                if (future.isSuccess()) {
                    cf.complete(result);
                } else {
                    cf.completeExceptionally(future.cause());
                }
            });
            consumer.accept(result, promise);
            return cf;
        }

        CompletableFuture.runAsync(() -> {
            R result = supplier.get();
            ChannelPromise promise = context.newPromise().addListener(future -> {
                if (future.isSuccess()) {
                    cf.complete(result);
                } else {
                    cf.completeExceptionally(future.cause());
                }
            });
            consumer.accept(result, promise);
        }, eventLoop).whenComplete((v, cause) -> {
                    if (!Objects.isNull(cause)) {
                        cf.completeExceptionally(cause);
                    }
                });
        return cf;
    }

    /**
     * call consumer function apply of the {@link StreamReaderListener},
     * if null will apply {@code .defaultStreamListener}
     * @param stream http2Stream
     * @param consumer consumer function
     */
    private void applyStreamListener(Http2Stream stream, Consumer<StreamReaderListener> consumer) {
        StreamReaderListener listener = stream.removeProperty(streamListenerKey);
        if (Objects.isNull(listener)) {
            listener = defaultStreamListener;
        }
        consumer.accept(listener);
    }

    @Override
    public void close() {
        context.close().syncUninterruptibly();
    }
}
