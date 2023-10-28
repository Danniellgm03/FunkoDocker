package org.docker.common.utils;

import org.docker.common.models.Funko;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase que se encarga de generar un id único para cada funko
 * @see Funko
 * @author daniel
 */
public class IdGenerator {

    private long id = 1L;

    private final Lock lock = new ReentrantLock(true);

    private static IdGenerator instance;

    private IdGenerator() {
    }

    /**
     * Método que se encarga de devolver la instancia de la clase
     */
    public static IdGenerator getInstance() {
        if (instance == null) {
            instance = new IdGenerator();
        }
        return instance;
    }


    /**
     * Método que se encarga de devolver el id actual y de incrementar el valor del id
     */
    public Long getAndIncrementeId() {
        lock.lock();
        try {
            Long currentId = id;
            id++;
            return currentId;
        } finally {
            lock.unlock();
        }
    }

}
