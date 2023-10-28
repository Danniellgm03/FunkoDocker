package org.docker.server.services.cache;


import reactor.core.publisher.Mono;

/**
 * Interfaz para un cache
 */
public interface Cache<K, V> {

    /**
     * Guarda un valor en el cache
     * @param key - key
     * @param value - value
     *
     */
    Mono<Void> put(K key, V value) throws Exception;

    /**
     * Obtiene un valor del cache
     * @param key - key
     */
    Mono<V> get(K key);

    /**
     * Elimina un valor del cache
     * @param key- key
     */
    Mono<V> delete(K key);

    /**
     * Limpia el cache
     */
    void clear();

    /**
     * Cierra el cache
     */
    void shutdown();

}
