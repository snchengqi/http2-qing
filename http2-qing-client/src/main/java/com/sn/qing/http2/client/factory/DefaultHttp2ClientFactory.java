package com.sn.qing.http2.client.factory;

import com.sn.qing.http2.client.DefaultHttp2Client;
import com.sn.qing.http2.client.environment.Environment4Client;
import com.sn.qing.http2.client.Http2Client;
import com.sn.qing.http2.core.handler.NettyHttp2HandlerBuilder;

import java.net.InetSocketAddress;

/**
 * Default implement of {@link Http2Client} factory method
 * @author ChengQi
 * @date 2020-06-23 11:55
 */
public class DefaultHttp2ClientFactory implements Http2ClientFactory {

    private final Environment4Client env;

    DefaultHttp2ClientFactory(Environment4Client environment) {
        this.env = environment;
    }

    @Override
    public Http2Client createClient() {
        InetSocketAddress address = new InetSocketAddress(env.getIp(), env.getPort());
        NettyHttp2HandlerBuilder builder = new NettyHttp2HandlerBuilder()
                .server(false)
                .pushEnable(env.isPushEnable())
                .initialWindowSize(env.getWindowSize())
                .maxFrameSize(env.getMaxFrameSize())
                .maxHeaderListSize(env.getHeaderListSize())
                .maxHeaderTableSize(env.getHeaderTableSize())
                .heartbeatInterval(env.getHeartbeatInterval())
                .heartbeatTimeout(env.getHeartbeatTimeout());
        return new DefaultHttp2Client(address, env.isSslEnable(), env.getCrtPath(),
                env.getConnectTimeoutMills(), builder, env.getReconnectPolicy(), env.getServerPushHandler());
    }
}
