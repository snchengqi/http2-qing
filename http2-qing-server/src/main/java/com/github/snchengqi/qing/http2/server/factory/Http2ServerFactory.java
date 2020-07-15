package com.github.snchengqi.qing.http2.server.factory;

import com.github.snchengqi.qing.http2.server.Http2Server;

/**
 * {@link Http2Server} factory method
 * @author ChengQi
 * @date 2020-06-24
 */
public interface Http2ServerFactory {

    /**
     * factory method which is used to create a new {@link Http2Server} instance
     * @return http2Server
     */
    Http2Server createServer();
}
