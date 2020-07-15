package com.github.snchengqi.qing.http2.core.connection;

import com.github.snchengqi.qing.http2.core.StreamReaderListener;
import com.github.snchengqi.qing.http2.core.invoker.StreamWriterInvoker;
import com.github.snchengqi.qing.http2.core.invoker.StreamReaderInvoker;

import java.io.Closeable;

/**
 * The abstract interface of HTTP2 connection,
 * maintain {@link State} itself and propagate to {@link Listener}.
 * It provides some write method and read for callback.
 * @author ChengQi
 * @date 2020-06-18
 */
public interface Connection extends StreamReaderInvoker, StreamWriterInvoker, Closeable {

    /**
     * Listener for the http2 connection
     */
    interface Listener extends Comparable<Listener> {

        /**
         * It will be called when trigger connection`s state changed
         * @param connection the http2 connection
         * @param state state of the connection
         */
        void onStateChange(Connection connection, State state);

        @Override
        default int compareTo(Listener o) {
            return this.hashCode() - o.hashCode();
        }
    }

    /**
     * The state of connection
     */
    enum State {

        /**
         * The object of connection has been created in memory,
         * but the channel not be active yet.
         */
        INITIAL,
        /**
         * It means the channel has be active and can calls written method
         */
        CREATED,
        /**
         * The connection has been closed
         */
        CLOSED
    }

    /**
     * register a new listener and then will be called when state change
     * @param listener the listener of connection
     */
    void addListener(Listener listener);

    /**
     * unregister listener
     * @param listener the listener of connection
     */
    void removeListener(Listener listener);

    /**
     * register the default {@link StreamReaderListener} for reveal all the details
     * @param defaultStreamListener StreamReaderListener
     */
    void setDefaultStreamListener(StreamReaderListener defaultStreamListener);

    /**
     * get the current state of http2 connection
     * @return state
     */
    State state();

    /**
     * set the current state of http2 connection
     * @param state state
     */
    void state(State state);

    /**
     * called when happen connection exception
     * @param cause error
     */
    void onError(Throwable cause);
}
