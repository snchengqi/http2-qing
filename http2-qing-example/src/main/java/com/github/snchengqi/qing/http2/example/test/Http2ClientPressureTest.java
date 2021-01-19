package com.github.snchengqi.qing.http2.example.test;

import com.github.snchengqi.qing.http2.client.Http2Client;
import com.github.snchengqi.qing.http2.client.environment.Environment4Client;
import com.github.snchengqi.qing.http2.client.factory.Http2ClientFactory;
import com.github.snchengqi.qing.http2.client.factory.Http2ClientFactoryBuilder;
import com.github.snchengqi.qing.http2.core.entity.HttpRequest;
import com.github.snchengqi.qing.http2.core.entity.HttpResponse;
import io.netty.handler.codec.http2.EmptyHttp2Headers;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ChengQi
 * @date 2021/1/19
 */
public class Http2ClientPressureTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Environment4Client env = new Environment4Client();
        env.setIp("127.0.0.1")
                .setPort(8080)
                .setServerPushHandler(context -> {});
        Http2ClientFactoryBuilder builder = new Http2ClientFactoryBuilder(env);
        Http2ClientFactory factory = builder.build();
        try(Http2Client client = factory.createClient()) {
            CompletableFuture<Void> connectFuture = client.connect();
            connectFuture.get();
            long start = System.currentTimeMillis();
            AtomicInteger count = new AtomicInteger();
            while (count.get() <= 10_000_000) {
                String requestContent = "request message of client";
                HttpRequest request = new HttpRequest(EmptyHttp2Headers.INSTANCE, requestContent.getBytes());
                CompletableFuture<HttpResponse> sendFuture = client.sendRequest(request);
                sendFuture.whenComplete((response, cause) -> {
                    if (cause == null) {
                        System.out.println("time:" + new Date());
                        System.out.println("client send request message:" + requestContent);
                        System.out.println("receive server response:" + new String(response.getBody()));
                    } else {
                        cause.printStackTrace();
                    }
                    int countSend = count.incrementAndGet();
                    if (countSend % 1000 == 0) {
                        System.out.println("TPS: " + countSend * 1000 / (System.currentTimeMillis() - start));
                    }
                });
                Thread.sleep(1);
            }
        }
    }
}
