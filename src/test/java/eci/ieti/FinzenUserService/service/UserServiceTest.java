package eci.ieti.FinzenUserService.service;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void whenUserExistsByGoogleId_thenReturnExistingUser() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        User existing = new User("google123", "Alice", "alice@test.com");
        existing.setId(1L);

        Mockito.when(repo.findByGoogleId("google123")).thenReturn(Optional.of(existing));

        UserService service = new UserService(repo);
        User result = service.findOrCreateUser("google123", "Alice", "alice@test.com");

        assertEquals(existing, result);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void whenUserExistsByEmail_thenReturnExistingUser() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        User existing = new User("google999", "Bob", "bob@test.com");
        existing.setId(2L);

        Mockito.when(repo.findByGoogleId("google123")).thenReturn(Optional.empty());
        Mockito.when(repo.findByEmail("bob@test.com")).thenReturn(Optional.of(existing));

        UserService service = new UserService(repo);
        User result = service.findOrCreateUser("google123", "Bob", "bob@test.com");

        assertEquals(existing, result);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void whenUserNotExists_thenSaveNewUser() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        Mockito.when(repo.findByGoogleId("newGoogleId")).thenReturn(Optional.empty());
        Mockito.when(repo.findByEmail("new@test.com")).thenReturn(Optional.empty());

        Mockito.when(repo.save(Mockito.any(User.class)))
                .thenAnswer(inv -> {
                    User u = inv.getArgument(0);
                    u.setId(3L);
                    return u;
                });

        UserService service = new UserService(repo);
        User result = service.findOrCreateUser("newGoogleId", "Charlie", "new@test.com");

        assertNotNull(result.getId());
        assertEquals("Charlie", result.getName());
        Mockito.verify(repo).save(Mockito.any(User.class));
    }

    @Test
    void findById_returnsUserWhenPresent() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        User existing = new User("g", "Dora", "dora@test.com");
        existing.setId(4L);

        Mockito.when(repo.findById(4L)).thenReturn(Optional.of(existing));

        UserService service = new UserService(repo);
        Optional<User> result = service.findById(4L);

        assertTrue(result.isPresent());
        assertEquals("Dora", result.get().getName());
    }
}