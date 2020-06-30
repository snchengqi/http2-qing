package com.sn.qing.http2.example;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sn.qing.http2.core.entity.HttpPushAck;
import com.sn.qing.http2.core.entity.HttpPushEntity;
import com.sn.qing.http2.core.entity.HttpRequest;
import com.sn.qing.http2.core.entity.HttpResponse;
import com.sn.qing.http2.server.Http2Server;
import com.sn.qing.http2.server.connection.ConnectionFacade;
import com.sn.qing.http2.server.connection.manager.SimpleConnectionManager;
import com.sn.qing.http2.server.environment.Environment4Server;
import com.sn.qing.http2.server.factory.Http2ServerFactory;
import com.sn.qing.http2.server.factory.Http2ServerFactoryBuilder;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2Stream;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author ChengQi
 * @date 2020-06-28
 */
public class Http2ServerEntry {

    private static final ScheduledExecutorService EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("server-push-%d").build());

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        SimpleConnectionManager connectionManager = new SimpleConnectionManager();
        Environment4Server env = new Environment4Server();
        env.setPort(8080).setConnectionManager(connectionManager).setRequestHandler(context -> {
            if (context.isSuccess()) {
                ConnectionFacade connection = context.connection();
                Http2Stream stream = context.stream();
                HttpRequest request = context.request();
                String responseMessage = "server response for client request";
                System.out.println("=============================");
                System.out.println("time:" + new Date());
                System.out.println("server receive client request message:" + new String(request.getBody()));
                System.out.println("stream id:" + stream.id());
                System.out.println("server send response message:" + responseMessage);
                System.out.println("=============================");

                HttpResponse response = new HttpResponse(EmptyHttp2Headers.INSTANCE, responseMessage.getBytes());
                connection.sendResponse(response, stream.id());
                return;
            }
            context.cause().printStackTrace();
        });

        Http2ServerFactory factory = new Http2ServerFactoryBuilder(env).build();

        try(Http2Server server = factory.createServer()) {
            CompletableFuture<Void> bindFuture = server.start();
            bindFuture.get();

            String pushMessage = "push message of server";
            EXECUTOR_SERVICE.scheduleAtFixedRate(() -> connectionManager.forEach(connection -> {
                System.out.println("server start to send a push message to client at time:" + new Date());
                CompletableFuture<HttpPushAck> future = connection.sendPushMessage(new HttpPushEntity(EmptyHttp2Headers.INSTANCE,
                        pushMessage.getBytes()));
                future.whenComplete((r, cause) -> {
                   if (cause == null) {
                       System.out.println("=============================");
                       System.out.println("time:" + new Date());
                       System.out.println("server send push message:" + pushMessage);
                       System.out.println("receive client ack:" + new String(r.getBody()));
                       System.out.println("=============================");
                       return;
                   }
                   cause.printStackTrace();
                });
            }), 1, 1, TimeUnit.SECONDS);

            Thread.sleep(3600000);
            EXECUTOR_SERVICE.shutdown();
        }
    }
}
