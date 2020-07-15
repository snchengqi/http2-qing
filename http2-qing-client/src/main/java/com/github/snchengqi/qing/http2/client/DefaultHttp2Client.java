package com.github.snchengqi.qing.http2.client;

import com.github.snchengqi.qing.http2.core.entity.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.github.snchengqi.qing.http2.client.initializer.Http2ClientInitializer;
import com.github.snchengqi.qing.http2.client.push.ServerPushContext;
import com.github.snchengqi.qing.http2.client.push.ServerPushHandler;
import com.github.snchengqi.qing.http2.client.reconnect.ReconnectPolicy;
import com.github.snchengqi.qing.http2.core.ConnectionPostProcessor;
import com.github.snchengqi.qing.http2.core.StreamReaderListener;
import com.github.snchengqi.qing.http2.core.connection.Connection;
import com.github.snchengqi.qing.http2.core.handler.NettyHttp2HandlerBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.Http2Stream;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty default implement of {@link Http2Client} interface
 * @author ChengQi
 * @date 2020-06-22
 */
public class DefaultHttp2Client implements Http2Client {

    private static final ReconnectPolicy DEFAULT_RECONNECT_POLICY = connectTimes -> 5000;

    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isNormalClosed = new AtomicBoolean(false);
    private AtomicInteger reconnectTimes;
    private ReconnectPolicy reconnectPolicy = DEFAULT_RECONNECT_POLICY;
    private ScheduledExecutorService executorService;

    private final Bootstrap bootstrap;
    private final EventLoopGroup loopGroup;
    private final SocketAddress address;

    private Connection connection;

    private final ServerPushHandler pushHandler;

    private final ClientConnectionPostProcessor postProcessor;

    public DefaultHttp2Client(SocketAddress address, boolean sslEnable, String crtPath,
                              int connectTimeout, NettyHttp2HandlerBuilder builder,
                              ReconnectPolicy reconnectPolicy, ServerPushHandler pushHandler) {
        if (!Objects.isNull(reconnectPolicy)) {
            this.reconnectPolicy = reconnectPolicy;
        }
        if (this.reconnectPolicy.supportReconnect()) {
            executorService = new ScheduledThreadPoolExecutor(1,
                    new ThreadFactoryBuilder().setNameFormat("reconnect-%d").build());
            reconnectTimes = new AtomicInteger();
        }
        this.pushHandler = pushHandler;
        this.postProcessor = new ClientConnectionPostProcessor(new ServerPushStreamDistributor(),
                new ConnectionCreatedListener());
        this.address = address;
        builder.connectionPostProcessor(postProcessor);
        bootstrap = new Bootstrap();
        loopGroup = new NioEventLoopGroup();
        bootstrap.group(loopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .handler(new Http2ClientInitializer(builder, sslEnable, crtPath));
    }

    @Override
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> connectFuture = new CompletableFuture<>();
        Connection.Listener notifyListener = (connection, state) -> {
          if (state == Connection.State.CREATED) {
              connectFuture.complete(null);
          } else if (state == Connection.State.CLOSED){
              connectFuture.completeExceptionally(new IOException("connection has been closed"));
          }
        };
        postProcessor.replaceNotifyListener(notifyListener);
        bootstrap.connect(address).addListener(future -> {
           if (!future.isSuccess()) {
               connectFuture.completeExceptionally(future.cause());
           }
        });
        return connectFuture;
    }

    private void reconnectIfNeed() {
        try {
            if (!isConnected.get() && !isNormalClosed.get() && reconnectPolicy.supportReconnect()) {
                doReconnect();
            } else if (!isConnected.get()) {
                close0();
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private void doReconnect() throws IOException {
        int reconnectInterval;
        try {
            reconnectInterval = reconnectPolicy.nextReconnectInterval(reconnectTimes.getAndIncrement());
        } catch (Exception e) {
            close0();
            return;
        }
        postProcessor.replaceStateListener(new ConnectionCreatedListener());
        executorService.schedule(() -> {
            connect().exceptionally(cause -> {
                reconnectIfNeed();
                return null;
            });
        }, reconnectInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<HttpResponse> sendRequest(HttpRequest request) {
        if (Objects.isNull(request)) {
            throw new IllegalArgumentException("parameter: request must`t be null");
        }
        checkConnectionState();
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        CompletableFuture<StreamMessage> sendFuture = connection.writeStreamMessage(request.toStreamMessage());
        sendFuture.whenComplete((r, cause) -> {
            if (Objects.isNull(cause)) {
                future.complete(new HttpResponse(r));
            } else {
                future.completeExceptionally(cause);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> sendRequestOneWay(HttpRequest request) {
        if (Objects.isNull(request)) {
            throw new IllegalArgumentException("parameter: request must`t be null");
        }
        checkConnectionState();
        return connection.writeStreamMessage(request.toStreamMessage(), null);
    }

    @Override
    public CompletableFuture<Void> sendPushAck(HttpPushAck pushAck, int streamId) {
        if (Objects.isNull(pushAck)) {
            throw new IllegalArgumentException("parameter: pushAck must`t be null");
        }
        if (streamId <= 0) {
            throw new IllegalArgumentException("parameter: streamId must`t be gather than 0");
        }
        checkConnectionState();
        return connection.writeStreamMessage(pushAck.toStreamMessage(), streamId);
    }

    private void checkConnectionState() {
        if (Objects.isNull(connection) || !isConnected.get()) {
            throw new IllegalStateException("connection is not been active on http2 protocol");
        }
    }

    @Override
    public void close() throws IOException {
        isNormalClosed.getAndSet(true);
        close0();
    }

    private void close0() throws IOException {
        if (!Objects.isNull(connection)) {
            connection.close();
        }
        if (!Objects.isNull(executorService)) {
            executorService.shutdown();
        }
        loopGroup.shutdownGracefully();
    }

    /**
     * {@link ConnectionPostProcessor} for http2 client, after the connection initialized,
     * it will be called to change the connection
     */
    private class ClientConnectionPostProcessor implements ConnectionPostProcessor {

        private ServerPushStreamDistributor distributor;
        private Connection.Listener connectionStateListener;
        private Connection.Listener connectNotifyListener;

        ClientConnectionPostProcessor(ServerPushStreamDistributor distributor,
                                      Connection.Listener connectionStateListener) {
            this.distributor = distributor;
            this.connectionStateListener = connectionStateListener;
        }

        @Override
        public void afterInitializing(Connection connection) {
            connection.setDefaultStreamListener(distributor);
            connection.addListener(connectionStateListener);
            connection.addListener(connectNotifyListener);
        }

        void replaceNotifyListener(Connection.Listener notifyListener) {
            connectNotifyListener = notifyListener;
        }

        void replaceStateListener(Connection.Listener stateListener) {
            connectionStateListener = stateListener;
        }
    }

    /**
     * {@link Connection.Listener}
     * to manage created state of connection
     */
    private class ConnectionCreatedListener implements Connection.Listener {

        @Override
        public void onStateChange(Connection connection, Connection.State state) {
            if (state == Connection.State.CREATED) {
                DefaultHttp2Client.this.isConnected.getAndSet(true);
                DefaultHttp2Client.this.connection = connection;
                DefaultHttp2Client.this.reconnectTimes = new AtomicInteger();
                connection.removeListener(this);
                connection.addListener(new ConnectionClosedListener());
            }
        }
    }

    /**
     * {@link Connection.Listener}
     * to manage closed state of connection when the connection is closed not normally
     */
    private class ConnectionClosedListener implements Connection.Listener {

        @Override
        public void onStateChange(Connection connection, Connection.State state) {
            if (state == Connection.State.CLOSED) {
                DefaultHttp2Client.this.isConnected.getAndSet(false);
                DefaultHttp2Client.this.connection = null;
                reconnectIfNeed();
            }
        }
    }

    /**
     * implement of {@link StreamReaderListener} which is used to
     * distribute for business program handling server push message
     */
    private class ServerPushStreamDistributor implements StreamReaderListener {

        @Override
        public void onStreamMessageRead(Connection connection, Http2Stream stream, StreamMessage streamMessage) {
            ServerPushContext context = ServerPushContext.create(DefaultHttp2Client.this,
                    stream, new HttpPushEntity(streamMessage), null);
            pushHandler.onReceived(context);
        }

        @Override
        public void onStreamError(Connection connection, Http2Stream stream, Throwable cause) {
            ServerPushContext context = ServerPushContext.create(DefaultHttp2Client.this,
                    stream, null, cause);
            pushHandler.onReceived(context);
        }
    }
}
