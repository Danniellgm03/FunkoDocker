package services;

import org.docker.common.enums.Modelo;
import org.docker.server.exceptions.cache.CachePutNullKeyException;
import org.docker.server.exceptions.cache.CachePutNullValueException;
import org.docker.common.models.Funko;
import org.docker.server.services.funkos.FunkoCacheImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FunkoCacheImplTest {

    private FunkoCacheImpl cache;

    @BeforeEach
    void setUp(){
        cache = new FunkoCacheImpl(15, 1, 1, TimeUnit.MINUTES);
    }

    @Test
    void getTest(){
        Funko funko = new Funko(1, UUID.randomUUID(), 123456L,  "Mi Funko", Modelo.MARVEL, 50.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());


        cache.put(funko.getId(), funko).block();

        assertAll(
                () -> assertEquals(funko, cache.get(funko.getId()).block()),
                () -> assertNull(cache.get(20).block())
        );
    }


    @Test
    void putTest(){
        Funko funko = new Funko(null, UUID.randomUUID(), 123456L,  "Mi Funko", Modelo.MARVEL, 50.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        assertAll(
                () -> assertThrowsExactly(CachePutNullKeyException.class, () -> cache.put(funko.getId(), funko).block()),
                () -> assertThrowsExactly(CachePutNullValueException.class, () -> cache.put(4,null).block())
        );
    }

    @Test
    void cleannerAutoCache(){
        Funko funko= new Funko(2, UUID.randomUUID(), 1256L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        funko.setUpdated_at(LocalDateTime.now().minusMinutes(3));
        cache = new FunkoCacheImpl(10, 1, 1, TimeUnit.MILLISECONDS);
        cache.put(funko.getId(), funko).block();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(cache.get(funko.getId()).block());
    }

    @Test
    void putUpdateFunko(){
        Funko funko= new Funko(2, UUID.randomUUID(), 1256L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());

        LocalDateTime fecha_test = LocalDateTime.of(2023, Month.OCTOBER,7, 19, 24);
        funko.setUpdated_at(fecha_test);
        cache.put(funko.getId(), funko).block();
        assertEquals(fecha_test, cache.get(funko.getId()).block().getUpdated_at());
    }

    @Test
    void deleteTest(){
        Funko funko = new Funko(2, UUID.randomUUID(), 1L,  "Mi Funko 2", Modelo.ANIME, 55.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        Funko funko2 = new Funko(10, UUID.randomUUID(), 1256L,  "Mi Funko", Modelo.MARVEL, 5.0, LocalDate.now(),  LocalDateTime.now(), LocalDateTime.now());
        cache.put(funko.getId(), funko).block();
        cache.put(funko2.getId(), funko2).block();
        cache.delete(funko.getId()).block();

        assertAll(
                () -> assertNull(cache.get(funko.getId()).block()),
                () -> assertEquals(funko2, cache.get(funko2.getId()).block()),
                () -> assertEquals(funko2, cache.delete(funko2.getId()).block()),
                () -> assertNull(cache.delete(4).block())
        );
    }



}
