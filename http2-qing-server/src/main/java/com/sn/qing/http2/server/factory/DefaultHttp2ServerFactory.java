package com.sn.qing.http2.server.factory;

import com.sn.qing.http2.core.handler.NettyHttp2HandlerBuilder;
import com.sn.qing.http2.server.DefaultHttp2Server;
import com.sn.qing.http2.server.Http2Server;
import com.sn.qing.http2.server.environment.Environment4Server;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;

/**
 * Default implement of {@link Http2Server} factory method
 * @author ChengQi
 * @date 2020-06-24
 */
public class DefaultHttp2ServerFactory implements Http2ServerFactory {

    private Environment4Server env;

    DefaultHttp2ServerFactory(Environment4Server env) {
        this.env = env;
    }

    @Override
    public Http2Server createServer() {
        InetSocketAddress address = StringUtil.isNullOrEmpty(env.getIp()) ?
                new InetSocketAddress(env.getPort()) : new InetSocketAddress(env.getIp(), env.getPort());
        NettyHttp2HandlerBuilder builder = new NettyHttp2HandlerBuilder()
                .server(true)
                .initialWindowSize(env.getWindowSize())
                .maxFrameSize(env.getMaxFrameSize())
                .maxHeaderListSize(env.getHeaderListSize())
                .maxHeaderTableSize(env.getHeaderTableSize())
                .heartbeatInterval(env.getHeartbeatInterval())
                .heartbeatTimeout(env.getHeartbeatTimeout());
        return new DefaultHttp2Server(address, env.isSslEnable(), env.getCrtPath(), env.getPrivateKeyPath(),
                builder, env.getRequestHandler(), env.getConnectionManager());
    }
}
