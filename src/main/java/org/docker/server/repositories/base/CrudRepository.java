package org.docker.server.repositories.base;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;

/**
 * CrudRepository con genericos, interfaz para un crud asincrono
 */
public interface CrudRepository<T, ID, EX extends Exception> {

    /**
     * Guarda un T en la base de datos
     */
    Mono<T> save(T t) throws SQLException, EX;

    /**
     * Actualiza un T en la base de datos
     */
    Mono<T> update(T t) throws SQLException, EX;

    /**
     * Busca un T por su id
     */
    Mono<T> findById(ID id) throws SQLException;

    /**
     * Busca todos los T de la base de datos
     */
    Flux<T> findAll() throws SQLException;

    /**
     * Elimina un T por su id
     */
    Mono<Boolean> deleteById(ID id) throws SQLException;


    /**
     * Elimina todos los T de la base de datos
     */
    Mono<Void> deleteAll() throws SQLException;

}
