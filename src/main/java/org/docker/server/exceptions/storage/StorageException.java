package org.docker.server.exceptions.storage;

public abstract class StorageException  extends RuntimeException{
    StorageException(String msg){
        super(msg);
    }
}
