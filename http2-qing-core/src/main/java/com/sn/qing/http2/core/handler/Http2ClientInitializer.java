package com.sn.qing.http2.core.handler;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;

import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * @author ChengQi
 * @date 2020-06-23 10:59
 */
public class Http2ClientInitializer extends AbstractHttp2Initializer {

    private String crtPath;

    public Http2ClientInitializer(NettyHttp2HandlerBuilder builder, boolean sslEnable, String crtPath) {
        super(builder, sslEnable);
        this.crtPath = crtPath;
    }

    @Override
    protected SslContext configSslContext() throws Exception {
        final SslProvider provider =
                SslProvider.isAlpnSupported(SslProvider.OPENSSL)? SslProvider.OPENSSL : SslProvider.JDK;
        return SslContextBuilder.forClient()
                .sslProvider(provider)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .trustManager(getTrustManagerFactory())
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                .build();
    }

    private TrustManagerFactory getTrustManagerFactory() throws CertificateException,
            IOException, KeyStoreException, NoSuchAlgorithmException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate ca;
        try(InputStream in = this.getClass().getResourceAsStream(crtPath)) {
            ca = factory.generateCertificate(in);
        }
        String keyType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyType);
        keyStore.setCertificateEntry("certificate", ca);
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        trustManagerFactory.init(keyStore);
        return trustManagerFactory;
    }
}
