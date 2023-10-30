package org.docker.server.services.storage;

import org.docker.common.models.Funko;
import org.docker.server.services.files.CsvManager;
import org.docker.server.services.files.JsonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Clase que implementa la interfaz FunkoStorageServ y que se encarga de
 * el almacenamiento de los datos en formato JSON y CSV
 * @see FunkoStorageServ
 * @see CsvManager
 * @see JsonManager
 * @see Funko
 * @author daniel
 */
public class FunkoStorageServImpl implements FunkoStorageServ{

    private static FunkoStorageServImpl instance;

    private final CsvManager csvManager;
    private final JsonManager jsonManager;

    private File backupDirectory;

    private Logger logger = LoggerFactory.getLogger(FunkoStorageServImpl.class);


    private FunkoStorageServImpl(CsvManager csvManager, JsonManager jsonManager) {
        this.csvManager = csvManager;
        this.jsonManager = jsonManager;

        System.out.println("INIT");
        System.out.println(initDirectories());

    }

    /**
     * Método que se encarga de crear el directorio para almacenar los backups
     */
    private boolean initDirectories(){
        String absolute_path = Paths.get("").toAbsolutePath().toString();
        backupDirectory = new File(absolute_path + File.separator + "backups");
        boolean existDirectory = backupDirectory.exists();
        System.out.println(backupDirectory.toString());
        if(!existDirectory){
            logger.debug("Creamos directorio para backups");
            return backupDirectory.mkdirs();
        }
        return existDirectory;
    }

    /**
     * Método que se encarga de crear una instancia de la clase
     * @param csvManager
     * @param jsonManager
     */
    public static FunkoStorageServImpl getInstance(CsvManager csvManager, JsonManager jsonManager) {
        if(instance == null){
            instance = new FunkoStorageServImpl(csvManager, jsonManager);
        }
        return instance;
    }

    /**
     * Metodo que se encarga de exportar los datos a un archivo JSON
     * @param data
     */
    public Mono<Boolean> exportToJsonAsync(List<Funko> data) throws ExecutionException, InterruptedException {
        logger.debug("Exportando a datos a JSON");
        String name_file = File.separator + "backupJsonFunkos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".json";
        return jsonManager.writeFunkosToJson(data, backupDirectory.getAbsolutePath() + name_file);
    }

    /**
     * Metodo que se encarga de importar los datos a un archivo CSV
     * @param filePath
     */
    @Override
    public Flux<Funko> importFromCsvAsync(String filePath) throws ExecutionException, InterruptedException {
        logger.debug("Importando datos desde csv");
        return csvManager.readCsv(filePath);
    }
}
