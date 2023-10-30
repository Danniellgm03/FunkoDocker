package org.docker.common.models;

public record Request(Type type, String content,  String token, String createdAt) {
    public enum Type {
        LOGIN, SALIR, GETALL, GETBYCOD, GETBYMODELO, GETBYCREATEDAT, POST, UPDATE, DELETE, DELETEALL
    }
}