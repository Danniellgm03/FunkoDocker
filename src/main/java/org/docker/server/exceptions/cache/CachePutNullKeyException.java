package org.docker.server.exceptions.cache;

public class CachePutNullKeyException extends RuntimeException{
    public CachePutNullKeyException(String msg) {
        super(msg);
    }
}
