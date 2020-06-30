package com.sn.qing.http2.core.handler;

import com.sn.qing.http2.core.ConnectionPostProcessor;
import com.sn.qing.http2.core.connection.Connection;
import com.sn.qing.http2.core.connection.DefaultConnection;
import io.netty.handler.codec.http2.AbstractHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Settings;

import java.util.Optional;

/**
 * A subclass to build {@link NettyHttp2Handler} which extends
 * {@link io.netty.handler.codec.http2.Http2ConnectionHandler}
 * @author ChengQi
 * @date 2020-06-19
 */
public class NettyHttp2HandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder<NettyHttp2Handler, NettyHttp2HandlerBuilder> {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<ConnectionPostProcessor> postProcessor = Optional.empty();
    private int heartbeatInterval = 5000;
    private int heartbeatTimeout = 60000;

    @Override
    protected NettyHttp2Handler build() {
        return super.build();
    }

    @Override
    protected NettyHttp2Handler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                      Http2Settings initialSettings) throws Exception {
        NettyHttp2Handler handler = new NettyHttp2Handler(decoder, encoder, initialSettings,
                heartbeatInterval, heartbeatTimeout);
        DefaultConnection connection = new DefaultConnection(encoder, decoder);
        frameListener(connection.getFrameListener());
        handler.setConnection(connection);
        handler.addChannelHandlerContextAware(connection);
        postProcessor.ifPresent(processor -> processor.afterInitializing(connection));
        connection.state(Connection.State.INITIAL);
        return handler;
    }

    /**
     * config a {@link ConnectionPostProcessor} function
     * @param postProcessor post processor
     * @return self
     */
    public NettyHttp2HandlerBuilder connectionPostProcessor(ConnectionPostProcessor postProcessor) {
        this.postProcessor = Optional.ofNullable(postProcessor);
        return self();
    }

    @Override
    public NettyHttp2HandlerBuilder server(boolean isServer) {
        return super.server(isServer);
    }

    /**
     * config heartbeat interval
     * @param heartbeatInterval heartbeat interval
     * @return self
     */
    public NettyHttp2HandlerBuilder heartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return self();
    }

    /**
     * config heartbeat timeout
     * @param heartbeatTimeout heartbeat timeout
     * @return self
     */
    public NettyHttp2HandlerBuilder heartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
        return self();
    }

    /**
     * config pushEnable
     * @param pushEnable pushEnable
     * @return self
     */
    public NettyHttp2HandlerBuilder pushEnable(boolean pushEnable) {
        this.initialSettings().pushEnabled(pushEnable);
        return self();
    }

    /**
     * config initial window size
     * @param initialWindowSize initial window size
     * @return self
     */
    public NettyHttp2HandlerBuilder initialWindowSize(int initialWindowSize) {
        this.initialSettings().initialWindowSize(initialWindowSize);
        return self();
    }

    /**
     * config max frame size
     * @param maxFrameSize max frame size
     * @return self
     */
    public NettyHttp2HandlerBuilder maxFrameSize(int maxFrameSize) {
        this.initialSettings().maxFrameSize(maxFrameSize);
        return self();
    }

    /**
     * config max header list size
     * @param maxHeaderListSize max header list size
     * @return self
     */
    public NettyHttp2HandlerBuilder maxHeaderListSize(long maxHeaderListSize) {
        this.initialSettings().maxHeaderListSize(maxHeaderListSize);
        return self();
    }

    /**
     * config max header table size
     * @param maxHeaderTableSize max header table size
     * @return self
     */
    public NettyHttp2HandlerBuilder maxHeaderTableSize(int maxHeaderTableSize) {
        this.initialSettings().headerTableSize(maxHeaderTableSize);
        return self();
    }
}
