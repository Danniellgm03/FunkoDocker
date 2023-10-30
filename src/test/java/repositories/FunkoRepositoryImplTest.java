package repositories;

import org.docker.common.enums.Modelo;
import org.docker.common.models.Funko;
import org.docker.server.repositories.funko.FunkoRepositoryImpl;
import org.docker.server.services.database.DataBaseManager;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class FunkoRepositoryImplTest {

    private DataBaseManager db;

    private FunkoRepositoryImpl repository;

    FunkoRepositoryImplTest() {
        db = DataBaseManager.getInstance();
        repository = FunkoRepositoryImpl.getInstance(db);
    }

    @AfterEach
    void tearDown() throws SQLException {
        repository.deleteAll().block();
    }

    @Test
    void findAll() throws SQLException{
        Funko funko = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        Funko funko2 = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        repository.save(funko).block();
        repository.save(funko2).block();

        var listFunkos = repository.findAll().collectList().block();

        assertAll(
                () -> assertTrue(listFunkos.size() == 2)
        );

    }

    @Test
    void saveTest() {
        Funko funko = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        assertAll(
                () -> assertEquals(funko, repository.save(funko).block()),
                () -> assertNull(repository.save(null).block())
        );

    }

    @Test
    void updateTest() throws SQLException {
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        Funko funko_inserted = repository.save(funko).block();
        funko.setPrecio(30.0);
        System.out.println(funko_inserted + " funko inserted");
        Funko funko_updated = repository.update(funko).block();
        System.out.println(funko_updated + " funko actualizado");


        assertAll(
                () ->  assertEquals(funko.getId(), funko_updated.getId()),
                () ->  assertEquals(funko.getPrecio(), funko_updated.getPrecio()),
                () ->  assertNull(repository.update(null).block())
        );
    }


    @Test
    void findById() throws SQLException {
        Funko funko = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        Funko funko_inserted = repository.save(funko).block();
        System.out.println(funko_inserted + " funko inserted");
        assertAll(
                () -> assertEquals(funko_inserted.getId(), repository.findById(funko_inserted.getId()).block().getId()),
                () -> assertNull(repository.findById(3).block())
        );
    }

    @Test
    void findByCod() throws SQLException {
        Funko funko = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        Funko funko_inserted = repository.save(funko).block();
        System.out.println(funko_inserted + " funko inserted");
        assertAll(
                () -> assertEquals(funko_inserted.getCOD(), repository.findByCod(funko_inserted.getCOD()).block().getCOD()),
                () -> assertNull(repository.findByCod(UUID.randomUUID()).block())
        );
    }

    @Test
    void deleteByIdTest() throws SQLException {
        Funko funko = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        Funko funko2 = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        repository.save(funko).block();
        Funko funko2_inserted = repository.save(funko2).block();
        System.out.println(repository.findAll().collectList().block());
        assertAll(
                () -> assertTrue(repository.deleteById(funko2_inserted.getId()).block()),
                () -> assertFalse(repository.deleteById(9999).block()),
                () -> assertTrue(repository.findAll().collectList().block().size() == 1)
        );

    }

    @Test
    void deleteAllTest() throws SQLException {
        Funko funko = new Funko(1, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        Funko funko2 = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        repository.save(funko).block();
        repository.save(funko2).block();

        repository.deleteAll().block();
        assertTrue(repository.findAll().collectList().block().size() == 0);
    }

    @Test
    void findByNombre() throws  SQLException {
        Funko funko = new Funko(null, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        repository.save(funko).block();

        assertAll(
                () -> assertTrue(repository.findByNombre("Mi Funko 2").collectList().block().size() == 1),
                () -> assertTrue(repository.findByNombre("Daniel").collectList().block().isEmpty())
        );
    }

}



