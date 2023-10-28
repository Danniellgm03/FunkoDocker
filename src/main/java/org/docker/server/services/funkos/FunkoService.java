package org.docker.server.services.funkos;


import org.docker.common.models.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.rmi.server.ExportException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Interfaz para el servicio de Funko
 * @author daniel
 */
public interface FunkoService {

    /**
     * Busca todos los funkos
     */
    Flux<Funko> findAll() throws SQLException, ExecutionException, InterruptedException;

    /**
     * Busca un funko por su nombre
     * @param nombre
     */
    Flux<Funko> findByNombre(String nombre) throws SQLException, ExecutionException, InterruptedException;

    /**
     * Busca un funko por su id
     * @param id
     */
    Mono<Funko> findById(Integer id) throws SQLException, ExecutionException, InterruptedException;

    /**
     * Busca un funko por su id
     * @param cod
     */
    Mono<Funko> findByCod(UUID cod) throws SQLException, ExecutionException, InterruptedException;


    /**
     * Guarda un funko en la base de datos
     * @param funko
     */
    Mono<Funko> save(Funko funko) throws Exception;


    /**
     * Actualiza un funko en la base de datos
     * @param funko
     */
    Mono<Funko> update(Funko funko) throws SQLException, ExecutionException, InterruptedException;

    /**
     * Elimina un funko por su id
     * @param id
     */
    Mono<Boolean> deleteById(Integer id) throws SQLException, ExecutionException, InterruptedException;

    /**
     * Elimina todos los funkos de la base de datos
     */
    Mono<Void> deleteAll() throws SQLException, ExecutionException, InterruptedException;

    /**
     * Realiza un backup de la base de datos
     */
    Mono<Boolean> backup() throws SQLException, ExecutionException, InterruptedException, ExportException;

    /**
     * Importa un csv a la base de datos
     */
    Flux<Funko> importCsv() throws ExecutionException, InterruptedException;

}
