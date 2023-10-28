package org.docker.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.docker.common.models.Request;
import org.docker.common.utils.adapters.LocalDateAdapter;
import org.docker.common.utils.adapters.LocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private PrintWriter out;
    private BufferedReader in;

    Socket socket;

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

            out.println(gson.toJson(new Request(Request.Type.GETALL, null, LocalDateTime.now().toString())));
            System.out.println(in.readLine());
            out.println(gson.toJson(new Request(Request.Type.GETBYCOD, UUID.fromString("fd6c6f58-7c6b-434b-82ab-001d2d6e4434").toString(), LocalDateTime.now().toString())));
            System.out.println(in.readLine());


            out.println(gson.toJson(new Request(Request.Type.SALIR, null, LocalDateTime.now().toString())));

            closeConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        this.socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("✅ Cliente conectado a " + HOST + ":" + PORT);
    }


    public void sendRequestGetAllFunkos() {
        Request request = new Request(Request.Type.GETALL, null, LocalDateTime.now().toString());
        System.out.println("Enviando petición: " + Request.Type.GETALL);
        logger.debug("Peticion Enviada: " + request);

    }


}
