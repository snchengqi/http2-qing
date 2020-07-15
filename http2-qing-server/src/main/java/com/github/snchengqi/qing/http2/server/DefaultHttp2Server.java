package com.github.snchengqi.qing.http2.server;

import com.github.snchengqi.qing.http2.core.ConnectionPostProcessor;
import com.github.snchengqi.qing.http2.core.StreamReaderListener;
import com.github.snchengqi.qing.http2.core.connection.Connection;
import com.github.snchengqi.qing.http2.core.entity.HttpRequest;
import com.github.snchengqi.qing.http2.core.entity.StreamMessage;
import com.github.snchengqi.qing.http2.core.handler.NettyHttp2HandlerBuilder;
import com.github.snchengqi.qing.http2.server.connection.SimpleConnectionFacade;
import com.github.snchengqi.qing.http2.server.connection.manager.ConnectionManager;
import com.github.snchengqi.qing.http2.server.initializer.Http2ServerInitializer;
import com.github.snchengqi.qing.http2.server.request.RequestContext;
import com.github.snchengqi.qing.http2.server.request.RequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2Stream;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty default implement of {@link Http2Server}
 * @author ChengQi
 * @date 2020-06-24
 */
public class DefaultHttp2Server implements Http2Server {

    private final AtomicInteger connectionCount = new AtomicInteger();

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workGroup;
    private final SocketAddress address;
    private ChannelFuture channelFuture;

    private final RequestHandler requestHandler;

    /**
     * the listener is used to count numbers of client connection
     */
    private final Connection.Listener innerConnectionListener = (connection, state) -> {
        if (state == Connection.State.CREATED) {
            connectionCount.incrementAndGet();
        } else if (state == Connection.State.CLOSED) {
            connectionCount.decrementAndGet();
        }
    };

    public DefaultHttp2Server(SocketAddress address, boolean sslEnable, String crtPath,
                              String privateKeyPath, NettyHttp2HandlerBuilder builder,
                              RequestHandler requestHandler, ConnectionManager connectionManager) {
        ConnectionPostProcessor connectionPostProcessor = connection -> {
            connection.setDefaultStreamListener(new Http2RequestDistributor());
            connection.addListener(innerConnectionListener);
            if (!Objects.isNull(connectionManager)) {
                connection.addListener(connectionManager);
            }
        };
        builder.connectionPostProcessor(connectionPostProcessor);
        this.requestHandler = requestHandler;
        this.address = address;
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new Http2ServerInitializer(builder, sslEnable, crtPath, privateKeyPath));
    }

    @Override
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> bindFuture = new CompletableFuture<>();
        channelFuture = bootstrap.bind(address);
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                bindFuture.complete(null);
            } else {
                bindFuture.completeExceptionally(future.cause());
            }
        });
        return bindFuture;
    }

    @Override
    public int connectionCount() {
        return connectionCount.get();
    }

    @Override
    public void close() {
        channelFuture.channel().close();
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    /**
     * the implement of {@link StreamReaderListener}
     * which is used to distribute the request of client
     */
    private class Http2RequestDistributor implements StreamReaderListener {

        @Override
        public void onStreamMessageRead(Connection connection, Http2Stream stream, StreamMessage streamMessage) {
            RequestContext context = RequestContext.create(new SimpleConnectionFacade(connection),
                    stream, new HttpRequest(streamMessage), null);
            requestHandler.onRequest(context);
        }

        @Override
        public void onStreamError(Connection connection, Http2Stream stream, Throwable cause) {
            RequestContext context = RequestContext.create(new SimpleConnectionFacade(connection),
                    stream, null, cause);
            requestHandler.onRequest(context);
        }
    }
}
