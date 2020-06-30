package com.sn.qing.http2.server.connection;

import com.sn.qing.http2.core.connection.Connection;
import com.sn.qing.http2.core.entity.HttpPushAck;
import com.sn.qing.http2.core.entity.HttpPushEntity;
import com.sn.qing.http2.core.entity.HttpResponse;
import com.sn.qing.http2.core.entity.StreamMessage;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Default implement of {@link ConnectionFacade}
 * @author ChengQi
 * @date 2020-06-24
 */
public class SimpleConnectionFacade implements ConnectionFacade {

    private final Connection connection;

    public SimpleConnectionFacade(Connection connection) {
        this.connection = connection;
    }

    @Override
    public CompletableFuture<Void> sendResponse(HttpResponse response, int streamId) {
        if (Objects.isNull(response)) {
            throw new IllegalArgumentException("parameter: response must`t be null");
        }
        if (streamId <= 0) {
            throw new IllegalArgumentException("parameter: streamId must`t be gather than 0");
        }
        return connection.writeStreamMessage(response.toStreamMessage(), streamId);
    }

    @Override
    public CompletableFuture<HttpPushAck> sendPushMessage(HttpPushEntity pushEntity) {
        if (Objects.isNull(pushEntity)) {
            throw new IllegalArgumentException("parameter: pushEntity must`t be null");
        }
        CompletableFuture<HttpPushAck> future = new CompletableFuture<>();
        CompletableFuture<StreamMessage> pushFuture = connection.writeStreamMessage(pushEntity.toStreamMessage());
        pushFuture.whenComplete((r, cause) -> {
            if (Objects.isNull(cause)) {
                future.complete(new HttpPushAck(r));
            } else {
                future.completeExceptionally(cause);
            }
        });
        return future;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleConnectionFacade that = (SimpleConnectionFacade) o;
        return Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection);
    }

    @Override
    public int compareTo(ConnectionFacade o) {
        SimpleConnectionFacade other = (SimpleConnectionFacade) o;
        return this.connection.hashCode() - other.connection.hashCode();
    }
}
