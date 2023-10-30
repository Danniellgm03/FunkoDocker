package org.docker.server.repositories.users;

import org.docker.common.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    private static UserRepository instance;


    private final List<User> users = List.of(
            new User(
                    1,
                    "admin",
                    BCrypt.hashpw("admin1234", BCrypt.gensalt(12)),
                    User.Role.ADMIN
            ),
            new User(
                    2,
                    "pepe",
                    BCrypt.hashpw("pepe1234", BCrypt.gensalt(12)),
                    User.Role.USER
            )
    );

    private UserRepository() {
    }

    public synchronized static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public Optional<User> findByByUsername(String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    public Optional<User> findByById(int id) {
        return users.stream()
                .filter(user -> user.id() == id)
                .findFirst();
    }
}
