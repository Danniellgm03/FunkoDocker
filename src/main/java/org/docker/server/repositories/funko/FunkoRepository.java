package org.docker.server.repositories.funko;

import org.docker.common.models.Funko;
import org.docker.server.repositories.base.CrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Interfaz para el repositorio de Funko
 * @see CrudRepository
 * @see Funko
 * @author daniel
 */

public interface FunkoRepository extends CrudRepository<Funko, Integer, SQLException> {

    /**
     * Busca un Funko por su nombre
     * @param name
     */
    Flux<Funko> findByNombre(String name) throws SQLException;

    /**
     * Busca un Funko por su cod
     * @param cod
     */
    Mono<Funko> findByCod(UUID cod) throws SQLException;

}
