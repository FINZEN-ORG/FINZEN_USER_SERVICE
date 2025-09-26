package eci.ieti.FinzenUserService.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import eci.ieti.FinzenUserService.dto.GoogleTokenDto;
import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.security.GoogleTokenVerifier;
import eci.ieti.FinzenUserService.security.JwtTokenProvider;
import eci.ieti.FinzenUserService.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @Test
    void authenticateWithGoogle_whenValidToken_returnsJwt() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        GoogleTokenVerifier verifier = Mockito.mock(GoogleTokenVerifier.class);
        JwtTokenProvider jwtProvider = Mockito.mock(JwtTokenProvider.class);

        AuthController controller = new AuthController(userService, verifier, jwtProvider);

        // Mock Google payload
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google123");
        payload.setEmail("alice@test.com");
        payload.set("name", "Alice");

        Mockito.when(verifier.verify("validToken")).thenReturn(payload);

        User user = new User("google123", "Alice", "alice@test.com");
        user.setId(1L);

        Mockito.when(userService.findOrCreateUser("google123", "Alice", "alice@test.com")).thenReturn(user);
        Mockito.when(jwtProvider.generateToken(user)).thenReturn("mockJwt");

        GoogleTokenDto dto = new GoogleTokenDto("validToken");

        ResponseEntity<?> response = controller.authenticateWithGoogle(dto);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("mockJwt"));
    }

    @Test
    void authenticateWithGoogle_whenInvalidToken_returns401() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        GoogleTokenVerifier verifier = Mockito.mock(GoogleTokenVerifier.class);
        JwtTokenProvider jwtProvider = Mockito.mock(JwtTokenProvider.class);

        AuthController controller = new AuthController(userService, verifier, jwtProvider);

        Mockito.when(verifier.verify("invalidToken")).thenReturn(null);

        GoogleTokenDto dto = new GoogleTokenDto("invalidToken");
        ResponseEntity<?> response = controller.authenticateWithGoogle(dto);

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void authenticateWithGoogle_whenVerifierThrows_returns500() throws Exception {
        UserService userService = Mockito.mock(UserService.class);
        GoogleTokenVerifier verifier = Mockito.mock(GoogleTokenVerifier.class);
        JwtTokenProvider jwtProvider = Mockito.mock(JwtTokenProvider.class);

        AuthController controller = new AuthController(userService, verifier, jwtProvider);

        Mockito.when(verifier.verify("boom")).thenThrow(new IOException("network error"));

        GoogleTokenDto dto = new GoogleTokenDto("boom");
        ResponseEntity<?> response = controller.authenticateWithGoogle(dto);

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Failed to verify"));
    }
}