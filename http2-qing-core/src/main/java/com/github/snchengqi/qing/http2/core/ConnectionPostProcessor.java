package com.github.snchengqi.qing.http2.core;

import com.github.snchengqi.qing.http2.core.connection.Connection;

/**
 * Connection initialization post processor
 * @author ChengQi
 * @date 2020-06-19
 */
@FunctionalInterface
public interface ConnectionPostProcessor {

    /**
     * when the connection for http2 has been constructed,
     * it will call the method to process the object
     * @param connection the connection has been constructed
     */
    void afterInitializing(Connection connection);
}
