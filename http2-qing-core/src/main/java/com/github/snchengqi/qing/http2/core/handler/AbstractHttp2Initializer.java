package com.github.snchengqi.qing.http2.core.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

/**
 * The subclass of {@link ChannelInitializer} for http2 protocol,
 * it is responsible for adding {@link SslHandler} and {@link NettyHttp2Handler}
 * to {@link ChannelPipeline}, it is also a template method class for client and server
 * @author ChengQi
 * @date 2020-06-23
 */
public abstract class AbstractHttp2Initializer extends ChannelInitializer<SocketChannel> {

    private NettyHttp2HandlerBuilder builder;
    private boolean sslEnable;

    public AbstractHttp2Initializer(NettyHttp2HandlerBuilder builder, boolean sslEnable) {
        this.builder = builder;
        this.sslEnable = sslEnable;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslEnable) {
            SslContext sslContext = configSslContext();
            SslHandler sslHandler = sslContext.newHandler(ch.alloc());
            pipeline.addFirst("sslHandler", sslHandler);
        }
        pipeline.addLast("http2Handler", builder.build());
    }

    /**
     * config ssl, generate sslContext
     * @return sslContext
     * @throws Exception if config ssl fail
     */
    protected abstract SslContext configSslContext() throws Exception;
}
