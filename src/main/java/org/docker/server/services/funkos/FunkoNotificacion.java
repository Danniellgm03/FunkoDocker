package org.docker.server.services.funkos;

import org.docker.common.models.Funko;
import org.docker.common.models.Notificacion;
import reactor.core.publisher.Flux;

/**
 * Interfaz para notificar a los subscriptores
 */
public interface FunkoNotificacion {

    /**
     * Obtiene la notificacion
     */
    Flux<Notificacion<Funko>> getNotificacion();

    /**
     * Notifica a los subscriptores
     * @param notify
     */
    void notify(Notificacion<Funko> notify);
}
