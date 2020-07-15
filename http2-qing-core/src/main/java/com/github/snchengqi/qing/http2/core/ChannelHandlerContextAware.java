package com.github.snchengqi.qing.http2.core;

import io.netty.channel.ChannelHandlerContext;

/**
 * {@link ChannelHandlerContext} callback interface
 * @author ChengQi
 * @date 2020-06-19
 */
public interface ChannelHandlerContextAware {

    /**
     * the method will be called after
     * {@link io.netty.handler.codec.http2.Http2ConnectionHandler}
     * being add to {@link io.netty.channel.ChannelPipeline}
     * @param ctx ChannelHandlerContext
     */
    void setChannelHandlerContext(ChannelHandlerContext ctx);
}
