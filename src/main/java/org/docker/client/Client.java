package org.docker.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.docker.common.enums.Modelo;
import org.docker.common.models.Funko;
import org.docker.common.models.Login;
import org.docker.common.models.Request;
import org.docker.common.models.Response;
import org.docker.common.utils.PropertiesReader;
import org.docker.common.utils.adapters.LocalDateAdapter;
import org.docker.common.utils.adapters.LocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.docker.common.models.Request.Type.GETALL;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private PrintWriter out;
    private BufferedReader in;

    private SSLSocket socket;

    String token;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public static void main(String[] args) {
        Client client = new Client();
        client.start();

    }

    public void start(){
        try {
            openConnection();

            token = sendRequestLogin("pepe", "pepe1234");

            Funko funko = new Funko(10, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

            sendRequestGetAllFunkos(token);

            sendRequestGetFunkoByCod(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"), token);

            sendRequestGetFunkoByModelo(Modelo.ANIME, token);

            sendRequestGetFunkoByCreatedAt("2023", token);

            sendRequestPostFunko(funko, token);

            funko.setPrecio(100.0);
            funko.setNombre("Mi Funko 2 Actualizado");
            sendRequestUpdateFunko(funko, token);

            sendRequestDeleteFunko(2840, token);



            sendRequestSalir(token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sendRequestLogin(String name_user, String pass){
        String token = null;

        var loginJson = gson.toJson(new Login(name_user, pass));
        Request request = new Request(Request.Type.LOGIN, loginJson, null, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.LOGIN);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        try {
            Response response = gson.fromJson(in.readLine(), Response.class);

            logger.debug("Respuesta recibida: " + response);

            switch (response.status()) {
                case TOKEN -> {
                    token = response.content();
                    System.out.println("🟢 Token: " + token);
                }
                default -> {
                    System.out.println("🔴 Error: " + response.content());
                }
            }
        } catch (IOException e) {
            logger.error("Error al leer la respuesta del servidor: " + e.getLocalizedMessage());
        }

        return token;
    }

    private void sendRequestSalir(String token) throws IOException {
        Request request = new Request(Request.Type.SALIR, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.SALIR);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        switch (response.status()) {
            case BYE -> {
                System.out.println("🟢 Bye: " + response.content());
                closeConnection();
            }
            case ERROR -> {
                System.out.println("🔴 Error: " + response.content());
            }
        }

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());
    }


    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        System.out.println("🔵 Cerrando Cliente");
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (socket != null)
            socket.close();
    }

    private void openConnection() throws IOException {
        System.out.println("🔵 Iniciando Cliente");
        logger.debug("Conectando al servidor: " + HOST + ":" + PORT);

        Map<String, String> myConfig = readConfigFile();

        logger.debug("Cargando fichero de propiedades");
        // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Debug
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile")); // llavero cliente
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword")); // clave

        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        logger.debug("Protocolos soportados: " + Arrays.toString(socket.getSupportedProtocols()));
        socket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.3"});

        logger.debug("Conectando al servidor: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("✅ Cliente conectado a " + HOST + ":" + PORT);
        infoSession(socket);

    }


    private void sendRequestGetAllFunkos(String token) throws IOException {

       Request request = new Request(GETALL, null, token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + GETALL);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                System.out.println("🟢 Los Funkos son: " + response.content());
            }
            case ERROR -> {
                System.out.println("🔴 Error: " + response.content());
            }

        }


    }

    private void sendRequestGetFunkoByCod(UUID uuid, String token) throws IOException {

        Request request = new Request(Request.Type.GETBYCOD, uuid.toString(), token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.GETBYCOD);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko funko = gson.fromJson(response.content(), Funko.class);
                System.out.println("🟢 Los Funkos son: " + funko);

            }
            case ERROR -> {
                System.out.println("🔴 Error: Funko no encontrado con cod:  " + uuid +"  "+ response.content());
            }
        }
    }

    public void sendRequestGetFunkoByModelo(Modelo modelo, String token) throws IOException {

        Request request = new Request(Request.Type.GETBYMODELO, modelo.toString(), token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.GETBYMODELO);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> funkos = gson.fromJson(response.content(), List.class);
                System.out.println("🟢 Los Funkos son: " + funkos);

            }
            case ERROR -> {
                System.out.println("🔴 Error: Funko no encontrado con modelo:  " + modelo +"  "+ response.content());
            }
        }
    }

    public void sendRequestGetFunkoByCreatedAt(String createdAt, String token) throws IOException {

        Request request = new Request(Request.Type.GETBYCREATEDAT, createdAt, token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.GETBYCREATEDAT);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> funkos = gson.fromJson(response.content(), List.class);
                System.out.println("🟢 Los Funkos son: " + funkos);

            }
            case ERROR -> {
                System.out.println("🔴 Error: Funko no encontrado con fecha de creación:  " + createdAt +"  "+ response.content());
            }
        }
    }

    public void sendRequestPostFunko(Funko funko, String token) throws IOException {

        Request request = new Request(Request.Type.POST, gson.toJson(funko), token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.POST);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko funko1 = gson.fromJson(response.content(), Funko.class);
                System.out.println("🟢 Funko insertado: " + funko1);

            }
            case ERROR -> {
                System.out.println("🔴 Error: El funko  " + funko +" no se pudo insertar "+ response.content());
            }
        }
    }

    public void sendRequestUpdateFunko(Funko funko, String token) throws IOException {

        Request request = new Request(Request.Type.UPDATE, gson.toJson(funko), token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.UPDATE);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko funko1 = gson.fromJson(response.content(), Funko.class);
                System.out.println("🟢 Funko actualizado: " + funko1);

            }
            case ERROR -> {
                System.out.println("🔴 Error: El funko  " + funko +" no se pudo actualizar "+ response.content());
            }
        }
    }

    public void sendRequestDeleteFunko(Integer id, String token) throws IOException {

        Request request = new Request(Request.Type.DELETE, String.valueOf(id), token,LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + Request.Type.DELETE);
        logger.debug("Peticion enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), Response.class);

        logger.debug("Respuesta recibida: " + response);
        System.out.println("Respuesta recibida con status: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko funko1 = gson.fromJson(response.content(), Funko.class);
                System.out.println("🟢 Funko eliminado: " + funko1);

            }
            case ERROR -> {
                System.out.println("🔴 Error: El funko  con id" + id +" no se pudo eliminar "+ response.content());
            }
        }
    }

    public Map<String, String> readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de configuracion");
            PropertiesReader properties = new PropertiesReader("client.properties");

            String keyFile = properties.getProperty("keyFile");
            String keyPassword = properties.getProperty("keyPassword");

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

            return configMap;
        } catch (FileNotFoundException e) {
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null; // Este retorno nunca se ejecutará debido a System.exit(1)
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuracion: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void infoSession(SSLSocket socket) {
        logger.debug("Información de la sesión");
        System.out.println("Información de la sesión");
        try {
            SSLSession session = socket.getSession();
            System.out.println("Servidor: " + session.getPeerHost());
            System.out.println("Cifrado: " + session.getCipherSuite());
            System.out.println("Protocolo: " + session.getProtocol());
            //System.out.println("Identificador:" + new BigInteger(session.getId()));
            System.out.println("Creación de la sesión: " + session.getCreationTime());
            X509Certificate certificado = (X509Certificate) session.getPeerCertificates()[0];
            System.out.println("Propietario : " + certificado.getSubjectX500Principal());
            System.out.println("Algoritmo: " + certificado.getSigAlgName());
            System.out.println("Tipo: " + certificado.getType());
            System.out.println("Número Serie: " + certificado.getSerialNumber());
            // expiración del certificado
            System.out.println("Válido hasta: " + certificado.getNotAfter());
        } catch (SSLPeerUnverifiedException ex) {
            logger.error("Error en la sesión: " + ex.getLocalizedMessage());
        }
    }


}
