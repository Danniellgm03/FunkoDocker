package services;

import org.docker.common.enums.Modelo;
import org.docker.server.exceptions.funko.FunkoNoEncontradoException;
import org.docker.common.models.Funko;
import org.docker.server.repositories.funko.FunkoRepositoryImpl;
import org.docker.server.services.funkos.FunkoCache;
import org.docker.server.services.funkos.FunkoNotificacionImpl;
import org.docker.server.services.funkos.FunkoServiceImpl;
import org.docker.server.services.storage.FunkoStorageServImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.rmi.server.ExportException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class FunkoServiceImplTest {

    @Mock
    FunkoRepositoryImpl repository;

    @Mock
    FunkoCache cache;

    @Mock
    FunkoStorageServImpl storageFunko;

    @Mock
    FunkoNotificacionImpl notificacion;

    @InjectMocks
    FunkoServiceImpl service;

    @Test
    void findAllTest() throws SQLException, ExecutionException, InterruptedException {
        List<Funko> funkos = new ArrayList<>();
        funkos.add(new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now()));
        funkos.add(new Funko(null, UUID.randomUUID(), 12L,  "Mi Funko prueba", Modelo.MARVEL, 25.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now()));

        when(repository.findAll()).thenReturn(Flux.fromIterable(funkos));

        var res = service.findAll().collectList().block();

        assertAll(
                () -> assertTrue(res.size() == 2),
                () -> assertEquals(res.get(1).getNombre(), "Mi Funko prueba")
        );
    }

    @Test
    void saveTest() throws Exception {
        Funko funko = new Funko(1, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        when(repository.save(funko)).thenReturn(Mono.just(funko));

        var res = service.save(funko).block();

        assertAll(
                () -> assertEquals(res.getNombre(), funko.getNombre())
        );

        verify(repository, times(1)).save(funko);
    }

    @Test
    void updateTest() throws Exception {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        when(repository.update(funko)).thenReturn(Mono.just(funko));
        when(repository.findById(funko.getId())).thenReturn(Mono.just(funko));
        when(cache.get(funko.getId())).thenReturn(Mono.empty());
        when(cache.put(funko.getId(), funko)).thenReturn(Mono.empty());

        var res = service.update(funko).block();
        assertAll(
                () -> assertEquals(funko.getNombre() , res.getNombre()),
                () -> assertEquals(funko.getId(), res.getId())
        );

        verify(repository, times(1)).update(funko);
    }


    @Test
    void updateNotExist() throws SQLException {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        when(repository.findById(funko.getId())).thenThrow(new FunkoNoEncontradoException("Funko no encontrado con el id: " + funko.getId()));

        try {
            var res = service.update(funko).block();
        } catch (ExecutionException | InterruptedException | FunkoNoEncontradoException e) {
            assertEquals(e.getMessage(), "Funko no encontrado con el id: " + funko.getId());
        }
        verify(repository, times(1)).findById(funko.getId());
    }


    @Test
    void findById() throws Exception {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        when(repository.findById(2)).thenReturn(Mono.just(funko));
        when(cache.get(2)).thenReturn(Mono.empty());
        when(cache.put(2, funko)).thenReturn(Mono.empty());

        var res = service.findById(2).block();

        assertAll(
                () -> assertEquals(res.getNombre(), funko.getNombre())
        );

        verify(repository, times(1)).findById(2);
    }

    @Test
    void findByCod() throws Exception {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        when(repository.findByCod(funko.getCOD())).thenReturn(Mono.just(funko));
        when(cache.put(any(), any())).thenReturn(Mono.empty());

        var res = service.findByCod(funko.getCOD()).block();

        assertAll(
                () -> assertEquals(res.getNombre(), funko.getNombre())
        );

        verify(repository, times(1)).findByCod(funko.getCOD());
        verify(cache, times(1)).put(any(), any());

    }


    @Test
    void findByIdNotExists() throws SQLException, ExecutionException, InterruptedException {
        when(repository.findById(2)).thenReturn(Mono.empty());
        when(cache.get(2)).thenReturn(Mono.empty());


        assertAll(
                () -> assertThrowsExactly(FunkoNoEncontradoException.class, () -> service.findById(2).block())
        );

        verify(repository, times(1)).findById(2);
    }


    @Test
    void deleteById() throws SQLException, ExecutionException, InterruptedException {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        when(repository.findById(2)).thenReturn(Mono.just(funko));
        when(repository.deleteById(2)).thenReturn(Mono.just(true));
        when(cache.delete(2)).thenReturn(Mono.empty());

        var res = service.deleteById(2).block();

        assertTrue(res);

        verify(repository, times(1)).deleteById(2);
    }


    @Test
    void deleteByIdNotExist() throws SQLException, ExecutionException, InterruptedException {

        when(repository.findById(2)).thenReturn(Mono.empty());

        assertThrowsExactly(FunkoNoEncontradoException.class, () -> service.deleteById(2).block());

        verify(repository, times(1)).findById(2);
    }



    @Test
    void deleteAllTest() throws SQLException, ExecutionException, InterruptedException {
        when(repository.deleteAll()).thenReturn(Mono.empty());

        service.deleteAll();

        verify(repository, times(1)).deleteAll();
    }


    @Test
    void findByNombreTest() throws SQLException, ExecutionException, InterruptedException {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        when(repository.findByNombre("Mi Funko 2")).thenReturn(Flux.just(funko));

        var res = service.findByNombre("Mi Funko 2").collectList().block();

        assertAll(
                () -> assertTrue(res.size() == 1),
                () -> assertEquals(funko.getNombre(), res.get(0).getNombre())
        );
    }

    @Test
    void backupTest() throws ExecutionException, InterruptedException, SQLException, ExportException, ExportException {
        List<Funko> funkos = new ArrayList<>();
        funkos.add(new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now()));
        funkos.add(new Funko(3, UUID.randomUUID(), 13L,  "Mi Funko 3", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now()));

        when(repository.findAll()).thenReturn(Flux.fromIterable(funkos));
        when(storageFunko.exportToJsonAsync(funkos)).thenReturn(Mono.just(true));

        assertTrue( service.backup().block() );
        verify(repository, times(1)).findAll();
        verify(storageFunko, times(1)).exportToJsonAsync(funkos);

    }

    @Test
    void importCsvTest() throws ExecutionException, InterruptedException, SQLException {
        List<Funko> funkos = new ArrayList<>();
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        Funko funko2 = new Funko(3, UUID.randomUUID(), 13L,  "Mi Funko 3", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        funkos.add(funko);
        funkos.add(funko2);

        Path path = Path.of("");
        String path_directory = path.toAbsolutePath().toString() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "data" + File.separator;
        String file = path_directory + "funkos.csv";
        when(storageFunko.importFromCsvAsync(file)).thenReturn(Flux.fromIterable(funkos));
        when(repository.save(funko)).thenReturn(Mono.just(funko));
        when(repository.save(funko2)).thenReturn(Mono.just(funko2));

        var res = service.importCsv();
        var funkosRes = res.collectList().block();

        assertAll(
                () -> assertTrue(funkosRes.size() == 2),
                () -> assertEquals(funko.getNombre(), funkosRes.get(0).getNombre()),
                () -> assertEquals(funko2.getNombre(), funkosRes.get(1).getNombre())
        );

    }


}
