package org.docker.server.services.funkos;

import org.docker.server.exceptions.cache.CachePutNullKeyException;
import org.docker.server.exceptions.cache.CachePutNullValueException;
import org.docker.common.models.Funko;
import org.docker.server.services.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementacion de la interfaz FunkoCache
 * @see FunkoCache
 * @see Cache
 * @see Funko
 * @author daniel
 */
public class FunkoCacheImpl implements FunkoCache {

    private final int maxSize;

    private Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);

    private final Map<Integer, Funko> cache;

    private final ScheduledExecutorService cleaner;

    /**
     * Constructor de FunkoCacheImpl
     * @param maxSize
     * @param initDelay
     * @param period
     * @param timeUnit
     */
    public FunkoCacheImpl(int maxSize, int initDelay, int period, TimeUnit timeUnit){
        this.maxSize = maxSize;
        this.cache =  new LinkedHashMap<>(maxSize, 0.75f, true ){
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Funko> eldest) {
                return size() > maxSize;
            }

        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, initDelay, period, timeUnit);
    }


    /**
     * Guarda un funko en la cache
     * @param key
     * @param value
     */
    @Override
    public Mono<Void> put(Integer key, Funko value) {
        logger.debug("Guardando funko: {} en la cache", value);
        if(key == null){
            logger.error("No se puede guardar un funko con id null en la cache");
            return Mono.error(new CachePutNullKeyException("No se puede guardar un funko con id null en la cache"));
        } else if (value == null) {
            logger.error("No se puede guardar un funko con value null en la cache");
            return Mono.error(new CachePutNullValueException("No se puede guardar un funko con value null en la cache"));
        }
        return Mono.fromRunnable(() -> cache.put(key, value));
    }

    /**
     * Obtiene un funko de la cache
     * @param key
     */
    @Override
    public Mono<Funko> get(Integer key) {
        logger.debug("Obteniendo funko con id: {} de la cache", key);
        return Mono.justOrEmpty(cache.get(key));
    }

    /**
     * Elimina un funko de la cache
     * @param key
     */
    @Override
    public Mono<Funko> delete(Integer key) {
        logger.debug("Eliminando funko con id: {} de la cache", key);
        return Mono.justOrEmpty(cache.remove(key));
    }

    /**
     * Elimina todos los funkos de la cache
     */
    @Override
    public void clear() {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getUpdated_at().plusSeconds(90).isBefore(LocalDateTime.now());
            if (shouldRemove) {
                logger.debug("Autoeliminando por caducidad alumno de cache con id: " + entry.getKey());
            }
            return shouldRemove;
        });
    }

    /**
     * Cierra el cleaner
     */
    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}
