package com.sn.qing.http2.server.factory;

import com.sn.qing.http2.core.Environment;
import com.sn.qing.http2.server.environment.Environment4Server;
import io.netty.util.internal.StringUtil;

import java.util.Objects;

/**
 * Builder class of {@link DefaultHttp2ServerFactory},
 * which is an implement of {@link Http2ServerFactory}
 * @author ChengQi
 * @date 2020-06-24
 */
public class Http2ServerFactoryBuilder {

    private final Environment4Server env;

    public Http2ServerFactoryBuilder(Environment4Server env) {
        this.env = Objects.requireNonNull(env, "parameter env can`t be null");
    }

    public Http2ServerFactory build() {
        checkEnvironment();
        return new DefaultHttp2ServerFactory(env);
    }

    /**
     * check {@link Environment} for server building
     */
    private void checkEnvironment() {
        if (Objects.isNull(env.getPort())) {
            throw new IllegalArgumentException("environment parameter: port must not be null");
        }
        if (env.isSslEnable()) {
            if (StringUtil.isNullOrEmpty(env.getCrtPath())) {
                throw new IllegalArgumentException("environment parameter: crtPath must not be empty when set sslEnable");
            }
            if (StringUtil.isNullOrEmpty(env.getPrivateKeyPath())) {
                throw new IllegalArgumentException("environment parameter: privateKeyPath must not be empty when set sslEnable");
            }
        }
        if (Objects.isNull(env.getRequestHandler())) {
            throw new IllegalArgumentException("environment parameter: requestHandler must not be empty when set sslEnable");
        }
        if (Objects.isNull(env.getConnectionManager())) {
            throw new IllegalArgumentException("environment parameter: connectionManager must not be empty when set sslEnable");
        }
        if (env.getWindowSize() <= 0) {
            throw new IllegalArgumentException("environment parameter: windowSize must be gather than 0");
        }
        if (env.getHeaderTableSize() <= 0) {
            throw new IllegalArgumentException("environment parameter: headerTableSize must be gather than 0");
        }
        if (env.getHeaderListSize() <= 0) {
            throw new IllegalArgumentException("environment parameter: headerListSize must be gather than 0");
        }
        if (env.getMaxFrameSize() <= 0) {
            throw new IllegalArgumentException("environment parameter: maxFrameSize must be gather than 0");
        }
        if (env.getMaxConcurrentStreams() <= 0) {
            throw new IllegalArgumentException("environment parameter: maxConcurrentStreams must be gather than 0");
        }
    }
}
