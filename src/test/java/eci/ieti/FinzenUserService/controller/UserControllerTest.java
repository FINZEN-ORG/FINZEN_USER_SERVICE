package eci.ieti.FinzenUserService.controller;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @Test
    void getCurrentUser_whenUserExists_returnsUserInfo() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        UserController controller = new UserController(repo);

        User user = new User("google123", "Alice", "alice@test.com");
        user.setId(1L);

        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(user));

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("1");

        ResponseEntity<?> response = controller.getCurrentUser(auth);

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Alice", body.get("name"));
        assertEquals("alice@test.com", body.get("email"));
    }

    @Test
    void getCurrentUser_whenUserNotFound_returns404() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        UserController controller = new UserController(repo);

        Mockito.when(repo.findById(99L)).thenReturn(Optional.empty());

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("99");

        ResponseEntity<?> response = controller.getCurrentUser(auth);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getCurrentUser_whenInvalidUserId_returns400() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        UserController controller = new UserController(repo);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("notANumber");

        ResponseEntity<?> response = controller.getCurrentUser(auth);

        assertEquals(400, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("error"));
    }
}