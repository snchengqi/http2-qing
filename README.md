# http2-qing
http2-qing is the encapsulation of http2 protocol based on netty

## Features

- Develop based on netty,with good performance
- Encapsulate the http2 protocol and provide simple and easy-to-use APIs
- Basically all asynchronous methods
- Support manage connections of server and server-push
- Provides client disconnection and reconnection and support customized reconnect policy

## How  to use

### program  codes  for  server

Construct the environment of server, and config necessary options. Firstly, you need to new a Environment4Server object, and then set propeties such as port,ConnectionManager,RequestHandler, as follows excample

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

Using Http2ServerFactoryBuilder to build the object which is a factory method object of Http2Server, so you can create a Http2Server Object, then start the server

```java
Http2ServerFactory factory = new Http2ServerFactoryBuilder(env).build();
Http2Server server = factory.createServer();
CompletableFuture<Void> bindFuture = server.start();
bindFuture.get();
```

And then, could get the connection by connectionManager  which you defined, and send server-push to remote endpoint, for example, like this

```java
connectionManager.forEach(connection -> {
    CompletableFuture<HttpPushAck> future = connection.sendPushMessage(HttpPushEntity(EmptyHttp2Headers.INSTANCE,pushMessage.getBytes()));
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

Finally, don`t forget close the Http2Server to release resources

```java
server.close();
```

### program  code  for   client

Construct the environment of client, and config necessary options. Firstly, you need to new a Environment4Client object, and then set propeties such as remote ip and port,ServerPushHandler, as follows excample

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

Using Http2ClientFactoryBuilder to build the object which is a factory method object of Http2Client, so you can create a Http2Client Object, then start it to connect to remote server

```java
Http2ClientFactory factory = new Http2ClientFactoryBuilder(env).build();
Http2Client client = factory.createClient();
CompletableFuture<Void> connectFuture = client.connect();
connectFuture.get();
```

After connected to server, you can user the client to send a request and receive response asynchronously

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

Finally，remember to close the client 

```java
client.close();
```

