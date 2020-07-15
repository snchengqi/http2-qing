package com.github.snchengqi.qing.http2.server.connection.manager;

import com.github.snchengqi.qing.http2.server.connection.ConnectionFacade;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Simple implement class of {@link ConnectionManager},
 * it will add connection to the set itself when the connection is created,
 * and remove it when closed
 * @author ChengQi
 * @date 2020-06-28
 */
public class SimpleConnectionManager extends ConcurrentSkipListSet<ConnectionFacade> implements ConnectionManager {

    /**
     * add {@link ConnectionFacade} to the connection manager
     * @param connection facade of the connection
     */
    public void addConnection(ConnectionFacade connection) {
        add(connection);
    }

    /**
     * remove {@link ConnectionFacade} from the connection manager
     * @param connection facade of the connection
     */
    public void removeConnection(ConnectionFacade connection) {
        remove(connection);
    }

    @Override
    public void onConnectionCreated(ConnectionFacade connection) {
        addConnection(connection);
    }

    @Override
    public void onConnectionClosed(ConnectionFacade connection) {
        removeConnection(connection);
    }
}
