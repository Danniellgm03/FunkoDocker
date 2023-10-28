package org.docker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.docker.common.models.Request;
import org.docker.common.utils.adapters.LocalDateAdapter;
import org.docker.common.utils.adapters.LocalDateTimeAdapter;
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

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientHandler extends Thread {

    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final long clientNumber;

    private final FunkoServiceImpl service;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket, long clientNumber, FunkoServiceImpl service) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
        this.service = service;

    }

    public void run(){
        try {
            openConnection();

            String inputLine;
            Request request;

            while(!clientSocket.isClosed()){
                inputLine = in.readLine();
                logger.debug("Recibido mensaje del cliente nº: " + clientNumber + " : " + inputLine);
                request = gson.fromJson(inputLine, Request.class);

                handleRequest(request);
            }

        } catch (IOException | SQLException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void openConnection() throws IOException {
        logger.debug("Conectando con el cliente nº: " + clientNumber + " : " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el cliente: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        out.close();
        in.close();
        clientSocket.close();
    }

    private void handleRequest(Request request) throws IOException, SQLException, ExecutionException, InterruptedException {
        logger.debug("Petición para procesar: " + request);
        switch (request.type()) {
            case SALIR -> closeConnection();
            case GETALL -> responseGetAll(request);
            case GETBYCOD -> responseGetByCod(request);
            case POST -> out.println("POST");
            case UPDATE -> out.println("UPDATE");
            case DELETE -> out.println("DELETE");
            default -> out.println("No se ha encontrado la petición");
        }
    }

    private void responseGetAll(Request request) throws SQLException, ExecutionException, InterruptedException {
        service.findAll().collectList().subscribe(
                funkos -> {
                    logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funkos);
                    out.println(gson.toJson(funkos));
                },
                error -> {
                    logger.error("Error al obtener los funkos: " + error.getMessage());
                    out.println("Error al obtener los funkos: " + error.getMessage());
                }
        );
    }

    private void responseGetByCod(Request request) throws SQLException, ExecutionException, InterruptedException {
        UUID cod = UUID.fromString(request.content());
        System.out.println("El cod es " + cod);
        service.findByCod(cod).subscribe(
                funko -> {
                    logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funko);
                    out.println(gson.toJson(funko));
                },
                error -> {
                    logger.error("Error al obtener el funko con cod: " + cod.toString() + " : " + error.getMessage());
                    out.println("Error al obtener el funko con cod: " + cod.toString() + " : " + error.getMessage());
                }
        );
    }


}
