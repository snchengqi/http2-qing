package com.sn.qing.http2.core;

import static io.netty.handler.codec.http2.Http2CodecUtil.*;
/**
 * The abstract class which is used to build client and server,
 * it has some universal options
 * @author ChengQi
 * @date 2020-06-23
 */
public abstract class Environment<E extends Environment<E>> {

    private int windowSize = DEFAULT_WINDOW_SIZE;
    private int headerTableSize = DEFAULT_HEADER_TABLE_SIZE;
    private long headerListSize = DEFAULT_HEADER_LIST_SIZE;
    private int maxFrameSize = DEFAULT_MAX_FRAME_SIZE;
    private int maxConcurrentStreams = SMALLEST_MAX_CONCURRENT_STREAMS;

    private int heartbeatInterval = 5000;
    private int heartbeatTimeout = 60000;

    private boolean sslEnable = false;
    private String crtPath;

    private String ip;
    private Integer port;

    /**
     * abstract method to return itself
     * @return itself
     */
    protected abstract E self();

    public int getWindowSize() {
        return windowSize;
    }

    public E setWindowSize(int windowSize) {
        this.windowSize = windowSize;
        return self();
    }

    public int getHeaderTableSize() {
        return headerTableSize;
    }

    public E setHeaderTableSize(int headerTableSize) {
        this.headerTableSize = headerTableSize;
        return self();
    }

    public long getHeaderListSize() {
        return headerListSize;
    }

    public E setHeaderListSize(long headerListSize) {
        this.headerListSize = headerListSize;
        return self();
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public E setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
        return self();
    }

    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    public E setMaxConcurrentStreams(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
        return self();
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public E setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return self();
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public E setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
        return self();
    }

    public String getCrtPath() {
        return crtPath;
    }

    public E setCrtPath(String crtPath) {
        this.crtPath = crtPath;
        return self();
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public E setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
        return self();
    }

    public String getIp() {
        return ip;
    }

    public E setIp(String ip) {
        this.ip = ip;
        return self();
    }

    public Integer getPort() {
        return port;
    }

    public E setPort(Integer port) {
        this.port = port;
        return self();
    }
}
