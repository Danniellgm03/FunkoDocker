package org.docker.server.services.files;

import org.docker.common.enums.Modelo;
import org.docker.common.models.Funko;
import org.docker.common.utils.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Clase que gestiona la lectura de un csv
 * @version 1.0
 * @author daniel
 */
public class CsvManager {
    Logger logger = LoggerFactory.getLogger(CsvManager.class);

    /**
     * Leemos el csv y lo convertimos en un flux de Funko
     * @param path
     */
    public Flux<Funko> readCsv(String path){
        IdGenerator idGenerator = IdGenerator.getInstance();
        return Flux.create(emitter -> {
            logger.debug("Leyendo el csv: " + path);

            try {
                List<String> lines = Files.readAllLines(Path.of(path));

                if(!lines.isEmpty()){
                    lines.remove(0);
                    for (String line: lines) {
                        String[] lines_split = line.split(",");

                        emitter.next(new Funko(
                                null,
                                UUID.fromString(lines_split[0].substring(0, 35)),
                                idGenerator.getAndIncrementeId(),
                                lines_split[1],
                                Modelo.valueOf(lines_split[2]),
                                Double.parseDouble(lines_split[3]),
                                LocalDate.parse(lines_split[4]),
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        ));
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            emitter.complete();
        });

    }


}
