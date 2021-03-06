package com.github.snchengqi.qing.http2.client.factory;

import com.github.snchengqi.qing.http2.client.Http2Client;

/**
 * {@link Http2Client} factory method
 * @author ChengQi
 * @date 2020-06-23
 */
public interface Http2ClientFactory {

    /**
     * factory method to create {@link Http2Client}
     * @return http2Client
     */
    Http2Client createClient();
}
