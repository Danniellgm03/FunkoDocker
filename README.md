# FunkoDocker


## Cliente

### Descripción

Este proyecto implementa un cliente para interactuar con un servidor Docker remoto. Permite realizar diversas operaciones, como autenticación, obtención de información sobre Funkos, inserción, actualización y eliminación de Funkos.

### Instrucciones de Uso

Asegúrate de tener las siguientes dependencias instaladas:

- Gson: Librería para manipular objetos JSON en Java.
- SLF4J: API para manejo de logs en Java.
- Logback: Implementación de SLF4J para escribir logs.


### Configuración de SSL
La comunicación con el servidor se realiza a través de SSL. Asegúrate de tener el certificado y la contraseña configurados correctamente en el archivo client.properties.



### Ejecución
Para ejecutar el cliente Docker, puedes utilizar el método start() en la clase Client.

``` java
public static void main(String[] args) {
    Client client = new Client();
    client.start();
}
```

### Funcionalidades Principales


- Autenticación: El método sendRequestLogin(String name_user, String pass) permite autenticarse en el servidor Docker.

- Obtener todos los Funkos: sendRequestGetAllFunkos(String token) obtiene una lista de todos los Funkos disponibles.

- Obtener Funko por Código: sendRequestGetFunkoByCod(UUID uuid, String token) busca un Funko por su código único.

- Obtener Funkos por Modelo: sendRequestGetFunkoByModelo(Modelo modelo, String token) recupera una lista de Funkos según su modelo.

- Obtener Funkos por Fecha de Creación: sendRequestGetFunkoByCreatedAt(String createdAt, String token) busca Funkos creados en una fecha específica.

- Insertar Funko: sendRequestPostFunko(Funko funko, String token) inserta un nuevo Funko en el servidor.

- Actualizar Funko: sendRequestUpdateFunko(Funko funko, String token) actualiza la información de un Funko existente.

- Eliminar Funko: sendRequestDeleteFunko(Integer id, String token) elimina un Funko por su ID.

- Salir: sendRequestSalir(String token) permite cerrar la sesión en el servidor.

## Servidor

Este proyecto contiene el código fuente para un servidor Docker que proporciona servicios relacionados con Funkos. El servidor está implementado en Java y utiliza sockets seguros (SSL) para la comunicación con los clientes.

### Estructura del Proyecto

El proyecto está organizado en varios paquetes:

- org.docker.server: Contiene las clases principales del servidor.
- org.docker.server.repositories: Repositorios para el acceso a datos.
- org.docker.server.services: Servicios para la gestión de Funkos y otros recursos.
- org.docker.server.services.files: Manejo de archivos en diferentes formatos (CSV, JSON).
- org.docker.server.services.funkos: Funcionalidades específicas de Funkos, como caché y notificaciones.
- org.docker.server.services.storage: Servicios de almacenamiento de Funkos.


### Configuración
El servidor requiere una configuración específica para funcionar correctamente:

- server.properties: Archivo de configuración del servidor. Contiene información sobre el keystore y la contraseña.

### Características

- Autenticación y Autorización: El servidor maneja la autenticación de usuarios y proporciona tokens de acceso. Los usuarios están autorizados según sus roles.

- Operaciones CRUD: Los clientes pueden realizar operaciones de Crear, Leer, Actualizar y Eliminar (CRUD) sobre Funkos.

- Seguridad: La comunicación entre el cliente y el servidor está protegida mediante SSL.

- Manejo de Excepciones: Se manejan diferentes tipos de excepciones, como errores de base de datos y errores de comunicación.

## SERVER DOCKER

Para poder ejecutar el servidor Docker deberas utilizar los siguientes comandos:

Contruir el docker: `docker-compose -f docker-compose.yaml up --build`

### JacocoTestReport

Para poder generar el reporte de Jacoco deberas utilizar los siguientes comandos:

`./gradlew clean build`

`./gradlew test jacocoTestReport`
