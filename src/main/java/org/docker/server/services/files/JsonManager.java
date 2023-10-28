package org.docker.server.services.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.docker.common.models.Funko;
import org.docker.common.utils.adapters.LocalDateAdapter;
import org.docker.common.utils.adapters.LocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase que gestiona la escritura de funkos en un json
 * @version 1.0
 * @author daniel
 */
public class JsonManager {

    private Logger logger = LoggerFactory.getLogger(JsonManager.class);

    /**
     * Escribe una lista de funkos en un json
     * @param funkos
     * @param path_output
     */
    public Mono<Boolean> writeFunkosToJson(List<Funko> funkos, String path_output)  {
        if(funkos == null  || funkos.isEmpty() || path_output == null){
            return Mono.just(false);
        }
        return Mono.defer(() -> {
            if(funkos == null){
                logger.error("No se puede exportar funkos porque es null");
                return Mono.just(false);
            }
            if(Files.exists(Path.of(path_output))){
                logger.error("No se puede expotar funkos a " + path_output + " porque no existe");
                return Mono.just(false);
            }

            logger.debug("Escribiendo funkos en un json");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .setPrettyPrinting()
                    .create();
            String json = gson.toJson(funkos);
            try {
                Path path_new_file = Files.writeString(new File(path_output).toPath(), json);
                return Mono.just(Files.exists(path_new_file));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
