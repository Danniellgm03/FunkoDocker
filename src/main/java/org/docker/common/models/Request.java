package org.docker.common.models;

public record Request(Type type, String content, String createdAt) {
    public enum Type {
        SALIR, GETALL, GETBYCOD, GETBYNOMBRE, POST, UPDATE, DELETE, DELETEALL
    }
}