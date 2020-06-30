package com.sn.qing.http2.server.connection.manager;

import com.sn.qing.http2.core.connection.Connection;
import com.sn.qing.http2.server.connection.ConnectionFacade;
import com.sn.qing.http2.server.connection.SimpleConnectionFacade;

/**
 * The manager interface of {@link Connection} is a sub interface of {@link Connection.Listener},
 * it implements {@code #onStateChange} method, wrap connection and calls methods itself.
 * If you need to manager connections by yourself, likes authentication, should implement the interface,
 * there is a default implement class
 * @see SimpleConnectionManager
 * @author ChengQi
 * @date 2020-06-24
 */
public interface ConnectionManager extends Connection.Listener {

    /**
     * default implements method of {@link Connection.Listener},
     * and forward to {@code #onConnectionCreated} and {@code #onConnectionClosed}
     * @param connection the http2 connection
     * @param state state of the connection
     */
    @Override
    default void onStateChange(Connection connection, Connection.State state) {
        SimpleConnectionFacade connectionFacade = new SimpleConnectionFacade(connection);
        switch (state) {
            case CREATED: onConnectionCreated(connectionFacade);
            break;
            case CLOSED: onConnectionClosed(connectionFacade);
            break;
            default: break;
        }
    }

    /**
     * called when {@link Connection} was created
     * @param connection http2 connection
     */
    void onConnectionCreated(ConnectionFacade connection);

    /**
     * called when {@link Connection} was closed
     * @param connection http2 connection
     */
    void onConnectionClosed(ConnectionFacade connection);
}
