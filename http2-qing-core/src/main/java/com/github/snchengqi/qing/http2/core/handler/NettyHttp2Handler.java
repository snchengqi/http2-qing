package com.github.snchengqi.qing.http2.core.handler;

import com.github.snchengqi.qing.http2.core.ChannelHandlerContextAware;
import com.github.snchengqi.qing.http2.core.connection.Connection;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A subclass of {@link Http2ConnectionHandler}, it providers idle check function
 * and notify {@link Connection} when state changed or reads frame
 * @author ChengQi
 * @date 2020-06-19
 */
public class NettyHttp2Handler extends Http2ConnectionHandler {

    private final Set<ChannelHandlerContextAware> contextAwareSet = new HashSet<>();
    private Connection connection;
    private final int heartbeatInterval;
    private final ReadOrWriteTimeHolder timeHolder;

    public NettyHttp2Handler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                             Http2Settings initialSettings, int heartbeatInterval, int heartbeatTimeout) {
        super(decoder, encoder, initialSettings);
        this.heartbeatInterval = heartbeatInterval;
        this.timeHolder = new ReadOrWriteTimeHolder(heartbeatTimeout);
    }

    public NettyHttp2Handler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                             Http2Settings initialSettings, boolean decoupleCloseAndGoAway,
                             int heartbeatInterval, int heartbeatTimeout) {
        super(decoder, encoder, initialSettings, decoupleCloseAndGoAway);
        this.heartbeatInterval = heartbeatInterval;
        this.timeHolder = new ReadOrWriteTimeHolder(heartbeatTimeout);
    }

    /**
     * add implement of {@link ChannelHandlerContextAware},
     * will set {@link ChannelHandlerContext} to them later
     * @param aware ChannelHandlerContextAware
     */
    public void addChannelHandlerContextAware(ChannelHandlerContextAware aware) {
        contextAwareSet.add(aware);
    }

    /**
     * set {@link Connection} as property itself
     * @param connection the abstract of connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * get the {@link Connection} property
     * @return the abstract of connection
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ctx.channel().pipeline().addFirst("ideaHandler",
                new IdleStateHandler(0, 0, heartbeatInterval, TimeUnit.MILLISECONDS));
        ctx.channel().pipeline().addAfter("ideaHandler", "readOrWriteTimeHolder",
                timeHolder);
        contextAwareSet.forEach(aware -> aware.setChannelHandlerContext(ctx));
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.flush();
    }

    @Override
    protected void onConnectionError(ChannelHandlerContext ctx, boolean outbound, Throwable cause,
                                     Http2Exception http2Ex) {
        super.onConnectionError(ctx, outbound, cause, http2Ex);
        connection.onError(cause);
        try {
            connection.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    @Override
    protected void onStreamError(ChannelHandlerContext ctx, boolean outbound, Throwable cause,
                                 Http2Exception.StreamException http2Ex) {
        super.onStreamError(ctx, outbound, cause, http2Ex);
        connection.onStreamError(connection().stream(http2Ex.streamId()), cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                if (timeHolder.isTimeout()) {
                    connection.onError(new IOException("channel idle, timeout:" + timeHolder.lastReadOrWriteTime));
                    connection.close();
                    return;
                }
                encoder().writePing(ctx, false, System.currentTimeMillis(), ctx.newPromise());
                flush(ctx);
            }
        }
    }

    /**
     * recorder for reading or writing time,
     * it will update variable {@code .lastReadOrWriteTime} when has inbound or outbound data
     */
    private static class ReadOrWriteTimeHolder extends ChannelDuplexHandler {

        private long lastReadOrWriteTime = System.currentTimeMillis();
        private final int heartbeatTimeout;
        private final ChannelFutureListener writeListener = future -> {
            if (future.isSuccess()) {
                this.lastReadOrWriteTime = System.currentTimeMillis();
            }
        };

        ReadOrWriteTimeHolder(int heartbeatTimeout) {
            this.heartbeatTimeout = heartbeatTimeout;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            ctx.write(msg, promise.unvoid()).addListener(writeListener);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            this.lastReadOrWriteTime = System.currentTimeMillis();
        }

        /**
         * is current connection timeout
         * @return {@code true} if timeout, {@code false} if not
         */
        boolean isTimeout() {
            return System.currentTimeMillis() - lastReadOrWriteTime > heartbeatTimeout;
        }
    }
}
