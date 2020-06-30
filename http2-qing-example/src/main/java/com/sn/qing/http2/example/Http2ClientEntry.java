package com.sn.qing.http2.example;

import com.sn.qing.http2.client.Http2Client;
import com.sn.qing.http2.client.environment.Environment4Client;
import com.sn.qing.http2.client.factory.Http2ClientFactory;
import com.sn.qing.http2.client.factory.Http2ClientFactoryBuilder;
import com.sn.qing.http2.core.entity.HttpPushAck;
import com.sn.qing.http2.core.entity.HttpPushEntity;
import com.sn.qing.http2.core.entity.HttpRequest;
import com.sn.qing.http2.core.entity.HttpResponse;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2Stream;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Example for show how to use http on client endpoint
 * @author ChengQi
 * @date 2020-06-28
 */
public class Http2ClientEntry {

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        Environment4Client env = new Environment4Client();
        env.setIp("127.0.0.1").setPort(8080).setServerPushHandler(context -> {
            if (context.isSuccess()) {
                Http2Client client = context.client();
                HttpPushEntity pushEntity = context.pushEntity();
                Http2Stream stream = context.stream();
                String ackMessage = "client ack for server push";
                System.out.println("=============================");
                System.out.println("time:" + new Date());
                System.out.println("client receive server push:" + (pushEntity.getBody() == null? "null": new String(pushEntity.getBody())));
                System.out.println("stream id:" + stream.id());
                System.out.println("client ack message:" + ackMessage);
                System.out.println("=============================");

                HttpPushAck ack = new HttpPushAck(EmptyHttp2Headers.INSTANCE, ackMessage.getBytes());
                client.sendPushAck(ack, stream.id());
                return;
            }
            context.cause().printStackTrace();
        });

        Http2ClientFactory factory = new Http2ClientFactoryBuilder(env).build();

        try(Http2Client client = factory.createClient()) {
            CompletableFuture<Void> connectFuture = client.connect();
            connectFuture.get();

            int loop = 0;
            while (loop++ <10) {
                String requestContent = "request message of client";
                HttpRequest request = new HttpRequest(EmptyHttp2Headers.INSTANCE, requestContent.getBytes());
                CompletableFuture<HttpResponse> requestFuture = client.sendRequest(request);
                requestFuture.whenComplete((r, cause) -> {
                    if (cause == null) {
                        System.out.println("=============================");
                        System.out.println("time:" + new Date());
                        System.out.println("client send request message:" + requestContent);
                        System.out.println("receive server response:" + new String(r.getBody()));
                        System.out.println("=============================");
                        return;
                    }
                    cause.printStackTrace();
                });
                Thread.sleep(1000);
            }

            Thread.sleep(600000);
        }
    }
}
