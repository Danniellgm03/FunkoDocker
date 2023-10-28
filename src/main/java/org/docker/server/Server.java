package org.docker.server;

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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private final Logger logger = LoggerFactory.getLogger(Server.class);

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
            ServerSocket serverSocket = new ServerSocket(3000);
            System.out.println("Servidor escuchando en el puerto 3000");

            while (true) {
                new ClientHandler(serverSocket.accept(), clientNumber.incrementAndGet(), service).start();
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
