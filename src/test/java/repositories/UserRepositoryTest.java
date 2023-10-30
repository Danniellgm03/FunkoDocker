package repositories;

import org.docker.common.models.User;
import org.docker.server.repositories.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {

    UserRepository repository = UserRepository.getInstance();


    @Test
    void findByByUsernameTest() {
        var user = repository.findByByUsername("admin").get();
        assertAll(
                () -> assertEquals("admin", user.username()),
                () -> assertTrue(BCrypt.checkpw("admin1234", user.password())),
                () -> assertEquals(User.Role.ADMIN, user.role())
        );
    }

    @Test
    void findByByIdTest() {
        var user = repository.findByById(1).get();
        assertAll(
                () -> assertEquals("admin", user.username()),
                () -> assertTrue(BCrypt.checkpw("admin1234", user.password())),
                () -> assertEquals(User.Role.ADMIN, user.role())
        );
    }

}
