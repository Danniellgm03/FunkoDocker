package org.docker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.docker.common.enums.Modelo;
import org.docker.common.models.*;
import org.docker.common.utils.adapters.LocalDateAdapter;
import org.docker.common.utils.adapters.LocalDateTimeAdapter;
import org.docker.server.repositories.funko.FunkoRepositoryImpl;
import org.docker.server.repositories.users.UserRepository;
import org.docker.server.services.Token.TokenService;
import org.docker.server.services.database.DataBaseManager;
import org.docker.server.services.files.CsvManager;
import org.docker.server.services.files.JsonManager;
import org.docker.server.services.funkos.FunkoCacheImpl;
import org.docker.server.services.funkos.FunkoNotificacionImpl;
import org.docker.server.services.funkos.FunkoServiceImpl;
import org.docker.server.services.storage.FunkoStorageServImpl;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
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

        }  catch (SQLException e) {
            logger.error("Error al conectar con la base de datos: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error al leer/escribir en el socket: " + e.getMessage());
        } catch (ExecutionException e) {
            logger.error("Error al ejecutar la operación: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Error al ejecutar la operación: " + e.getMessage());
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

    private void responseSalir() throws IOException {
        logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + "Adios");
        out.println(gson.toJson(new Response(Response.Status.BYE, "Adios", LocalDateTime.now().toString())));
        closeConnection();
    }

    private void handleRequest(Request request) throws IOException, SQLException, ExecutionException, InterruptedException {
        logger.debug("Petición para procesar: " + request);
        switch (request.type()) {
            case LOGIN -> responseLogin(request);
            case SALIR -> responseSalir();
            case GETALL -> responseGetAll(request);
            case GETBYCOD -> responseGetByCod(request);
            case GETBYMODELO -> responseGetByModel(request);
            case GETBYCREATEDAT -> responseGetByCreateDate(request);
            case POST -> responseCreateFunko(request);
            case UPDATE -> responseUpdateFunko(request);
            case DELETE -> responseDeleteFunko(request);
            default -> new Response(Response.Status.ERROR, "Petición no soportada", LocalDateTime.now().toString());
        }
    }

    private void responseLogin(Request request)  {
        Login login = gson.fromJson(request.content(), Login.class);
        Optional<User> user = UserRepository.getInstance().findByByUsername(login.username());

        if(user.isEmpty() || !BCrypt.checkpw(login.password(), user.get().password())){
            logger.error("Error al hacer login: " + login);
            out.println(gson.toJson(new Response(Response.Status.ERROR, "Error al hacer login: " + login, LocalDateTime.now().toString())));
        }

        var token = TokenService.getInstance().createToken(user.get(), Server.TOKEN_SECRET, Server.TOKEN_EXPIRATION);
        logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + token);
        out.println(gson.toJson(new Response(Response.Status.TOKEN, token, LocalDateTime.now().toString())));
    }

    private Optional<User> procesarToken(String token)  {
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET)) {
            logger.debug("Token válido");
            var claims = TokenService.getInstance().getClaims(token, Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt(); // es verdad que podríamos obtener otro tipo de datos
            var user = UserRepository.getInstance().findByById(id);
            if (user.isEmpty()) {
                logger.error("Usuario no autenticado correctamente");
                out.println(gson.toJson(new Response(Response.Status.ERROR, "Usuario no autenticado correctamente", LocalDateTime.now().toString())));
                return Optional.empty();
            }
            return user;
        } else {
            logger.error("Token no válido");
            out.println(gson.toJson(new Response(Response.Status.ERROR, "Token no válido", LocalDateTime.now().toString())));
            return Optional.empty();
        }
    }

    private void responseGetAll(Request request) throws SQLException, ExecutionException, InterruptedException {
        procesarToken(request.token());
        service.findAll().collectList().subscribe(
                funkos -> {
                    logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                },
                error -> {
                    logger.error("Error al obtener los funkos: " + error.getMessage());
                    out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al obtener los funkos: "+ error.getMessage(), LocalDateTime.now().toString())));
                }
        );
    }

    private void responseGetByCod(Request request) throws SQLException, ExecutionException, InterruptedException {
        procesarToken(request.token());
        UUID cod = UUID.fromString(request.content());
        service.findByCod(cod).subscribe(
                funko -> {
                    logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funko);
                    var resJson = gson.toJson(funko);
                    out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                },
                error -> {
                    logger.error("Error al obtener el funko con cod: " + cod.toString() + " : " + error.getMessage());
                    out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al obtener el funko con cod: " + cod.toString() + " : " + error.getMessage(), LocalDateTime.now().toString())));
                }
        );
    }

    private void responseGetByModel(Request request) throws SQLException, ExecutionException, InterruptedException {
        procesarToken(request.token());
        Modelo modelo = Modelo.valueOf(request.content());
        service.findAll().collectList()
                .map(funkos ->
                        funkos.stream()
                                .filter(funko -> funko.getModelo().equals(modelo)).toList()
                )
                .subscribe(
                        funkos -> {
                            logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funkos);
                            var resJson = gson.toJson(funkos);
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                        },
                        error -> {
                            logger.error("Error al obtener los funkos con modelo: " + modelo.toString() + " : " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al obtener los funkos con modelo: " + modelo.toString() + " : " + error.getMessage(), LocalDateTime.now().toString())));
                        }
                );
    }

    private void responseGetByCreateDate(Request request) throws SQLException, ExecutionException, InterruptedException {
        procesarToken(request.token());
        int year = Integer.parseInt(request.content());
        service.findAll().collectList()
                .map(funkos ->
                        funkos.stream()
                                .filter(funko -> funko.getFecha().getYear() == year).toList()
                ).subscribe(
                        funkos -> {
                            logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funkos);
                            var resJson = gson.toJson(funkos);
                            out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                        },
                        error -> {
                            logger.error("Error al obtener los funkos con fecha de lanzamiento: " + year + " : " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al obtener los funkos con fecha de lanzamiento: " + year + " : " + error.getMessage(), LocalDateTime.now().toString())));
                        }
                );
    }

    public void responseCreateFunko(Request request) throws SQLException {
            Funko funkoToSave = gson.fromJson(request.content(), Funko.class);
            service.saveWithoutNotify(funkoToSave).subscribe(
                    funko -> {
                        logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funko);
                        var resJson = gson.toJson(funko);
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                    },
                    error -> {
                        logger.error("Error al insertar el funko: " + funkoToSave + " : " + error.getMessage());
                        out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al insertar el funko: " + funkoToSave + " : " + error.getMessage(), LocalDateTime.now().toString())));
                    }
            );

    }

    public void responseUpdateFunko(Request request) throws  SQLException, ExecutionException, InterruptedException {

        Funko funkoToUpdate = gson.fromJson(request.content(), Funko.class);

        service.updateWithoutNotify(funkoToUpdate).subscribe(
                funko -> {
                    logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funko);
                    var resJson = gson.toJson(funko);
                    out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                },
                error -> {
                    logger.error("Error al actualizar el funko: " + funkoToUpdate + " : " + error.getMessage());
                    out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al actualizar el funko: " + funkoToUpdate + " : " + error.getMessage(), LocalDateTime.now().toString())));
                }
        );


    }

    public void responseDeleteFunko(Request request) throws SQLException {
        var user = procesarToken(request.token());
        if(user.isPresent() && user.get().role().equals(User.Role.ADMIN)) {
            int id = Integer.parseInt(request.content());
            service.deleteByIdWithoutNotification(id).subscribe(
                    funko -> {
                        logger.debug("Enviando respuesta al cliente nº: " + clientNumber + " : " + funko);
                        var resJson = gson.toJson(funko);
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                    },
                    error -> {
                        logger.error("Error al eliminar el funko con id: " + id + " : " + error.getMessage() + " " + error.toString());
                        out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al eliminar el funko con id: " + id + " : " + error.getMessage(), LocalDateTime.now().toString())));
                    }
            );
        }else{
            logger.error("Error al eliminar el funko: " + request.content() + " : " + "No tienes permisos para realizar esta acción");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"Error al eliminar el funko: " + request.content() + " : " + "No tienes permisos para realizar esta acción", LocalDateTime.now().toString())));
        }

    }
    


}
