package com.sn.qing.http2.server.initializer;

import com.sn.qing.http2.core.handler.AbstractHttp2Initializer;
import com.sn.qing.http2.core.handler.NettyHttp2HandlerBuilder;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;

import java.io.InputStream;

/**
 * It is used to add handler for netty http2 server
 * @author ChengQi
 * @date 2020-06-23
 */
public class Http2ServerInitializer extends AbstractHttp2Initializer {

    private String crtPath;
    private String privateKeyPath;

    public Http2ServerInitializer(NettyHttp2HandlerBuilder builder, boolean sslEnable,
                                  String crtPath, String privateKeyPath) {
        super(builder, sslEnable);
        this.crtPath = crtPath;
        this.privateKeyPath = privateKeyPath;
    }

    @Override
    protected SslContext configSslContext() throws Exception {
        try(InputStream crtIn = this.getClass().getResourceAsStream(crtPath);
            InputStream privateKeyIn = this.getClass().getResourceAsStream(privateKeyPath)) {
            final SslProvider provider =
                    SslProvider.isAlpnSupported(SslProvider.OPENSSL)? SslProvider.OPENSSL : SslProvider.JDK;
            return SslContextBuilder.forServer(crtIn, privateKeyIn)
                    .sslProvider(provider)
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2))
                    .build();
        }
    }
}
