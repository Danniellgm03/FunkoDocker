package org.docker.server.services.database;


import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.docker.common.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la conexión con la base de datos
 * Singleton
 * @version 1.0
 * @author daniel
 */
public class DataBaseManager {

    private static DataBaseManager instance;
    private Connection conn;

    private String url;
    private Boolean initCreateTables;

    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private final Logger logger = LoggerFactory.getLogger(DataBaseManager.class);

    private DataBaseManager(){
        initConfig();

        connectionFactory = ConnectionFactories.get(url);

        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(20)
                .build();

        pool = new ConnectionPool(configuration);

        if(initCreateTables){
            this.initTables();
        }
    }

    /**
     * Obtenemos instancia de la base de datos
     * @return this
     */
    public synchronized static DataBaseManager getInstance(){
        if(instance == null){
            instance = new DataBaseManager();
        }
        return instance;
    }


    /**
     * Inicializa las tablas de la base de datos
     */
    public synchronized void initTables() {
        logger.debug("Inicializando tablas de la base de datos");
        executeScript("init.sql").block();
        logger.debug("Tabla de la base de datos inicializada");
    }


    /**
     * Carga las propiedades de la base de datos
     */
    private synchronized void initConfig() {

        logger.debug("Cargando propiedades(conf) de la base de datos");
        //String propertiesFile = ClassLoader.getSystemResource("config.properties").getFile();
        //Properties props = new Properties();

        try {
            PropertiesReader props = new PropertiesReader("config.properties");
            //props.load(new FileInputStream(propertiesFile));
            url = props.getProperty("database.url");
            initCreateTables = props.getProperty("database.initTables").equals("true");
            url = props.getProperty("database.url");

            System.out.println(url);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Ejecuta un script de la base de datos
     * @param scriptSqlFile
     */
    public Mono<Void> executeScript(String scriptSqlFile) {
        logger.debug("Ejecutando script de inicialización de la base de datos: " + scriptSqlFile);
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    logger.debug("Creando conexión con la base de datos");
                    String scriptContent = null;
                    try {
                        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptSqlFile)) {
                            if (inputStream == null) {
                                return Mono.error(new IOException("No se ha encontrado el fichero de script de inicialización de la base de datos"));
                            } else {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                    scriptContent = reader.lines().collect(Collectors.joining("\n"));
                                }
                            }
                        }
                        Statement statement = connection.createStatement(scriptContent);
                        return Mono.from(statement.execute());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                },
                Connection::close
        ).then();
    }

    /**
     * Obtiene la conexión con la base de datos
     * @return ConnectionPoll
     */
    public ConnectionPool getConnectionPool() {
        return this.pool;
    }



}
