package com.github.snchengqi.qing.http2.client.factory;

import com.github.snchengqi.qing.http2.client.environment.Environment4Client;
import com.github.snchengqi.qing.http2.core.Environment;
import io.netty.util.internal.StringUtil;

import java.util.Objects;

/**
 * Builder class of {@link DefaultHttp2ClientFactory},
 * which is an implement of {@link Http2ClientFactory}
 * @author ChengQi
 * @date 2020-06-23
 */
public class Http2ClientFactoryBuilder {

    private final Environment4Client env;

    public Http2ClientFactoryBuilder(Environment4Client env) {
        this.env = Objects.requireNonNull(env, "parameter env can`t be null");
    }

    /**
     * build method
     * @return factory
     */
    public Http2ClientFactory build() {
        checkEnvironment();
        return new DefaultHttp2ClientFactory(env);
    }

    /**
     * check {@link Environment} for client building
     */
    private void checkEnvironment() {
        if (StringUtil.isNullOrEmpty(env.getIp())) {
            throw new IllegalArgumentException("environment parameter: ip must not be empty");
        }
        if (Objects.isNull(env.getPort())) {
            throw new IllegalArgumentException("environment parameter: port must not be null");
        }
        if (env.isSslEnable()) {
            if (StringUtil.isNullOrEmpty(env.getCrtPath())) {
                throw new IllegalArgumentException("environment parameter: crtPath must not be empty when set sslEnable");
            }
        }
        if (env.isPushEnable()) {
            if (Objects.isNull(env.getServerPushHandler())) {
                throw new IllegalArgumentException("environment parameter: serverPushHandler must not be null when set sslEnable");
            }
        }
        if (env.getConnectTimeoutMills() <= 0) {
            throw new IllegalArgumentException("environment parameter: connectTimeoutMills must be gather than 0");
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
