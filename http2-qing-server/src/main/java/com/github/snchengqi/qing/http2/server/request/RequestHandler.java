package com.github.snchengqi.qing.http2.server.request;

/**
 * The functional interface is use to handle request message
 * @author ChengQi
 * @date 2020-06-24
 */
@FunctionalInterface
public interface RequestHandler {

    /**
     * the method will be called when client send http2 request message
     * @param context http2 request context, contains connection stream and request message
     */
    void onRequest(RequestContext context);
}
