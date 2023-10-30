package org.docker.server;

import org.docker.common.utils.PropertiesReader;
import org.docker.server.repositories.funko.FunkoRepositoryImpl;
import org.docker.server.services.database.DataBaseManager;
import org.docker.server.services.files.CsvManager;
import org.docker.server.services.files.JsonManager;
import org.docker.server.services.funkos.FunkoCacheImpl;
import org.docker.server.services.funkos.FunkoNotificacionImpl;
import org.docker.server.services.funkos.FunkoServiceImpl;
import org.docker.server.services.storage.FunkoStorageServImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private static final Logger logger = LoggerFactory.getLogger(Server.class);


    public static final String TOKEN_SECRET = "DanielGarridoMurosIESLuisVives";
    public static final long TOKEN_EXPIRATION = 10000;

    private static final FunkoServiceImpl service = FunkoServiceImpl.getInstance(
            FunkoRepositoryImpl.getInstance(DataBaseManager.getInstance()),
            new FunkoCacheImpl(15, 1, 1, TimeUnit.MINUTES),
            FunkoNotificacionImpl.getInstance(),
            FunkoStorageServImpl.getInstance(
                    new CsvManager(),
                    new JsonManager()
            )
    );


    public static void main(String[] args) {
        try {

            var myConfig = readConfigFile();

            logger.debug("Configurando TSL");
            // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Depuramos
            System.setProperty("javax.net.ssl.keyStore", myConfig.get("keyFile")); // Llavero
            System.setProperty("javax.net.ssl.keyStorePassword", myConfig.get("keyPassword")); // Clave de acceso

            // Nos anunciamos como servidor de tipo SSL
            SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) serverFactory.createServerSocket(3000);

            // Opcionalmente podemos forzar el tipo de protocolo -> Poner el mismo que el cliente
            logger.debug("Protocolos soportados: " + Arrays.toString(serverSocket.getSupportedProtocols()));
            serverSocket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.3"});

            System.out.println("🚀 Servidor escuchando en el puerto 3000");

            while (true) {
                new ClientHandler(serverSocket.accept(), clientNumber.incrementAndGet(), service).start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static Map<String, String> readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de propiedades");
            PropertiesReader properties = new PropertiesReader("server.properties");

            String keyFile = properties.getProperty("keyFile");
            String keyPassword = properties.getProperty("keyPassword");
            String tokenSecret = properties.getProperty("tokenSecret");
            String tokenExpiration = properties.getProperty("tokenExpiration");
            // Comprobamos que no estén vacías
            if (keyFile.isEmpty() || keyPassword.isEmpty()) {
                throw new IllegalStateException("Hay errores al procesar el fichero de propiedades o una de ellas está vacía");
            }

            // Comprobamos el fichero de la clave
            if (!Files.exists(Path.of(keyFile))) {
                throw new FileNotFoundException("No se encuentra el fichero de la clave");
            }

            Map<String, String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);
            configMap.put("tokenSecret", tokenSecret);
            configMap.put("tokenExpiration", tokenExpiration);

            return configMap;
        } catch (FileNotFoundException e) {
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null; // Este retorno nunca se ejecutará debido a System.exit(1)
        } catch (IOException e) {
            logger.error("Error al leer el fichero de propiedades: " + e.getLocalizedMessage());
            return null;
        }
    }
}
