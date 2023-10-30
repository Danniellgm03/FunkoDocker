package services;

import com.auth0.jwt.interfaces.Claim;
import org.docker.common.models.User;
import org.docker.server.services.Token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {
    private TokenService tokenService;

    @BeforeEach
    public void setUp() {
        tokenService = TokenService.getInstance();
    }

    @Test
    public void testCreateToken() {
        User user = new User(1, "username", "password", User.Role.USER );
        String token = tokenService.createToken(user, "secret", 3600000L); // Reemplaza "secret" con tu secreto y 3600000L con la expiración deseada

        assertAll(
                () -> assertNotNull(token),
                () -> assertTrue(token.length() > 0)
        );

    }

    @Test
    public void testVerifyTokenWithValidTokenAndUser() {
        User user = new User(1, "username", "password", User.Role.USER );
        String token = tokenService.createToken(user, "secret", 3600000L);

        boolean result = tokenService.verifyToken(token, "secret", user);
        assertAll(
                () -> assertTrue(result)
        );
    }

    @Test
    public void testVerifyTokenWithValidToken() {
        User user = new User(1, "username", "password", User.Role.USER );

        String token = tokenService.createToken(user, "secret", 3600000L);

        boolean result = tokenService.verifyToken(token, "secret");

        assertAll(
                () -> assertTrue(result)
        );
    }

    @Test
    public void testVerifyTokenWithInvalidToken() {
        boolean result = tokenService.verifyToken("invalidToken", "secret");

        assertAll(
                () -> assertFalse(result)
        );
    }

    @Test
    public void testGetClaims() {
        User user = new User(1, "username", "password", User.Role.USER );

        String token = tokenService.createToken(user, "secret", 3600000L);

        java.util.Map<String, Claim> claims = tokenService.getClaims(token, "secret");

        assertAll(
                () -> assertNotNull(claims),
                () ->  assertTrue(claims.size() > 0)
        );
    }
}
