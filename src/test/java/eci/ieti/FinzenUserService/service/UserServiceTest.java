package eci.ieti.FinzenUserService.service;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User("google123", "Alice", "alice@test.com");
        existingUser.setId(1L);
    }

    @Test
    void findOrCreateUser_whenUserExistsByGoogleId_returnsExistingUser() {
        // Arrange
        when(userRepository.findByGoogleId("google123")).thenReturn(Optional.of(existingUser));

        // Act
        User result = userService.findOrCreateUser("google123", "Alice Updated", "alice@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(existingUser.getGoogleId(), result.getGoogleId());
        assertEquals("Alice", result.getName()); // Original name, not updated

        verify(userRepository, times(1)).findByGoogleId("google123");
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateUser_whenUserExistsByEmail_returnsExistingUser() {
        // Arrange
        User existingByEmail = new User("google999", "Bob", "bob@test.com");
        existingByEmail.setId(2L);

        when(userRepository.findByGoogleId("newGoogleId")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(existingByEmail));

        // Act
        User result = userService.findOrCreateUser("newGoogleId", "Bob Updated", "bob@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(existingByEmail.getId(), result.getId());
        assertEquals("google999", result.getGoogleId()); // Original googleId
        assertEquals("Bob", result.getName());
        assertEquals("bob@test.com", result.getEmail());

        verify(userRepository, times(1)).findByGoogleId("newGoogleId");
        verify(userRepository, times(1)).findByEmail("bob@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateUser_whenUserDoesNotExist_createsAndSavesNewUser() {
        // Arrange
        when(userRepository.findByGoogleId("newGoogleId")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L);
            return savedUser;
        });

        // Act
        User result = userService.findOrCreateUser("newGoogleId", "Charlie", "new@test.com");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(3L, result.getId());
        assertEquals("newGoogleId", result.getGoogleId());
        assertEquals("Charlie", result.getName());
        assertEquals("new@test.com", result.getEmail());

        verify(userRepository, times(1)).findByGoogleId("newGoogleId");
        verify(userRepository, times(1)).findByEmail("new@test.com");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals("newGoogleId", capturedUser.getGoogleId());
        assertEquals("Charlie", capturedUser.getName());
        assertEquals("new@test.com", capturedUser.getEmail());
    }

    @Test
    void findOrCreateUser_whenBothChecksReturnEmpty_createsNewUser() {
        // Arrange
        when(userRepository.findByGoogleId(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        // Act
        User result = userService.findOrCreateUser("unique123", "Test User", "unique@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findById_whenUserExists_returnsUserInOptional() {
        // Arrange
        User user = new User("g", "Dora", "dora@test.com");
        user.setId(4L);
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findById(4L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(4L, result.get().getId());
        assertEquals("Dora", result.get().getName());
        assertEquals("dora@test.com", result.get().getEmail());

        verify(userRepository, times(1)).findById(4L);
    }

    @Test
    void findById_whenUserDoesNotExist_returnsEmptyOptional() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void findOrCreateUser_withNullGoogleId_stillCreatesUser() {
        // Arrange
        when(userRepository.findByGoogleId(null)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        // Act
        User result = userService.findOrCreateUser(null, "No Google", "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertNull(result.getGoogleId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findOrCreateUser_withDifferentEmailAndGoogleId_createsNewUserWhenNoneExists() {
        // Arrange
        when(userRepository.findByGoogleId("newGoogle456")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("different@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(6L);
            return u;
        });

        // Act
        User result = userService.findOrCreateUser("newGoogle456", "Different User", "different@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("newGoogle456", result.getGoogleId());
        assertEquals("Different User", result.getName());
        assertEquals("different@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findById_withNullId_returnsEmptyOptional() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(null);
    }
}