# http2-qing
http2-qing是一个底层使用Netty封装的HTTP2.0协议库

![](https://img.shields.io/badge/license-Apache2-000000.svg)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/snchengqi/http2-qing)

## 特性

- 基于Netty开发，拥有很好的性能
- 对HTTP2协议的封装，提供了简单易用的API
- 基本上所有的方法都是异步的
- 支持连接全生命周期的管理和服务器推送（server-push）
- 提供了客户端断连重连机制，并且支持自定义重连策略

## 如何使用

### 服务端编码

首先，通过Maven构建工具将http2-qing-server模块的依赖引入到项目中。

```xml
<dependency>
  <groupId>com.github.snchengqi</groupId>
  <artifactId>http2-qing-server</artifactId>
  <version>1.0.1</version>
</dependency>
```

构造服务器环境，配置必须的启动参数。首先，需要创建一个Environment4Server 对象，创建好对象之后，设置比如端口，ConnectionManager，RequestHandler的属性，如下示例：

```java
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
```

利用Http2ServerFactoryBuilder 对象去构建一个Http2Server的工厂对象Http2ServerFactory，之后你就可以创建Http2Server 对象了，对象创建完成之后启动Http2Server。

```java
Http2ServerFactory factory = new Http2ServerFactoryBuilder(env).build();
Http2Server server = factory.createServer();
CompletableFuture<Void> bindFuture = server.start();
bindFuture.get();
```

之后你可以通过你先前定义好的connectionManager获取连接，并且可以通过server-push机制将消息推送到对端，示例如下：

```java
connectionManager.forEach(connection -> {
    CompletableFuture<HttpPushAck> future = connection.sendPushMessage(new HttpPushEntity(EmptyHttp2Headers.INSTANCE,pushMessage.getBytes()));
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
};
```

最后，当关闭程序时，不要忘记调用Http2Server的close方法来释放资源。

```java
server.close();
```

### 客户端编码

首先，通过Maven构建工具将http2-qing-client模块的依赖引入到项目中。

```xml
<dependency>
  <groupId>com.github.snchengqi</groupId>
  <artifactId>http2-qing-client</artifactId>
  <version>1.0.1</version>
</dependency>
```

构造客户端环境对象，并且配置必须的启动参数。首先，你需要创建一个Environment4Client对象，之后设置像服务器ip，端口和ServerPushHandler等属性，示例如下：

```java
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
```

利用Http2ClientFactoryBuilder对象来构建创建Http2Client的工厂方法对象Http2ClientFactory，之后你就可以创建Http2Client了，并且可以启动客户端连接到服务器。

```java
Http2ClientFactory factory = new Http2ClientFactoryBuilder(env).build();
Http2Client client = factory.createClient();
CompletableFuture<Void> connectFuture = client.connect();
connectFuture.get();
```

连接到服务器之后，你可以利用客户端异步地发送http2请求和接收服务器的响应消息。

```java
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
```

最后，记得关闭客户端以释放资源。

```java
client.close();
```

