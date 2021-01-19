package com.github.snchengqi.qing.http2.example.test;

import com.github.snchengqi.qing.http2.core.entity.HttpRequest;
import com.github.snchengqi.qing.http2.core.entity.HttpResponse;
import com.github.snchengqi.qing.http2.server.Http2Server;
import com.github.snchengqi.qing.http2.server.connection.ConnectionFacade;
import com.github.snchengqi.qing.http2.server.connection.manager.SimpleConnectionManager;
import com.github.snchengqi.qing.http2.server.environment.Environment4Server;
import com.github.snchengqi.qing.http2.server.factory.Http2ServerFactory;
import com.github.snchengqi.qing.http2.server.factory.Http2ServerFactoryBuilder;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2Stream;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author ChengQi
 * @date 2021/1/19
 */
public class Http2ServerPressureTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Environment4Server env = new Environment4Server();
        env.setPort(8080)
                .setConnectionManager(new SimpleConnectionManager())
                .setRequestHandler(context -> {
                    if (!context.isSuccess()) {
                        context.cause().printStackTrace();
                        return;
                    }
                    ConnectionFacade connection = context.connection();
                    HttpRequest request = context.request();
                    Http2Stream stream = context.stream();
                    System.out.println("time:" + new Date());
                    System.out.println("stream id:" + stream.id());
                    System.out.println("server receive client request message:" + new String(request.getBody()));
                    String responseMessage = "server response for client request";
                    HttpResponse response = new HttpResponse(EmptyHttp2Headers.INSTANCE, responseMessage.getBytes());
                    connection.sendResponse(response, stream.id());
                });
        Http2ServerFactory factory = new Http2ServerFactoryBuilder(env).build();
        try(Http2Server server = factory.createServer()) {
            CompletableFuture<Void> startFuture = server.start();
            startFuture.get();
            Thread.sleep(300000);
        }
    }
}
