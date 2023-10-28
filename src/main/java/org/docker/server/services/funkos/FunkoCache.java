package org.docker.server.services.funkos;


import org.docker.common.models.Funko;
import org.docker.server.services.cache.Cache;

/**
 * Interfaz para el cache de Funkos
 */
public interface FunkoCache extends Cache<Integer, Funko> {
}
