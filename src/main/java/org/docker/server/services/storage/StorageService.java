package org.docker.server.services.storage;

import org.docker.common.models.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Interfaz que se encarga de definir los métodos que se van a utilizar para el almacenamiento
 */
public interface StorageService<T> {

    /**
     * Método que se encarga de exportar los datos a un fichero JSON
     * @param data
     */
    Mono<Boolean> exportToJsonAsync(List<T> data) throws ExecutionException, InterruptedException;

    /**
     * Método que se encarga de exportar los datos a un fichero CSV
     * @param filePath
     */
    Flux<Funko> importFromCsvAsync(String filePath) throws ExecutionException, InterruptedException;


}
