package eci.ieti.FinzenUserService.controller;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User("google123", "Alice", "alice@test.com");
        mockUser.setId(1L);
    }

    @Test
    void getCurrentUser_whenUserExists_returnsUserInfoSuccessfully() {
        // Arrange
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1L, body.get("id"));
        assertEquals("Alice", body.get("name"));
        assertEquals("alice@test.com", body.get("email"));

        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getCurrentUser_whenUserNotFound_returns404() {
        // Arrange
        when(authentication.getName()).thenReturn("99");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(authentication, times(1)).getName();
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void getCurrentUser_whenInvalidUserId_returns400WithErrorMessage() {
        // Arrange
        when(authentication.getName()).thenReturn("notANumber");

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertTrue(errorBody.containsKey("error"));
        assertEquals("Invalid userId in JWT", errorBody.get("error"));

        verify(authentication, times(1)).getName();
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getCurrentUser_whenUserIdIsZero_handlesCorrectly() {
        // Arrange
        when(authentication.getName()).thenReturn("0");
        when(userRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findById(0L);
    }

    @Test
    void getCurrentUser_whenUserIdIsNegative_parsesAndSearches() {
        // Arrange
        when(authentication.getName()).thenReturn("-1");
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findById(-1L);
    }

    @Test
    void getCurrentUser_whenUserIdHasSpaces_throwsNumberFormatException() {
        // Arrange
        when(authentication.getName()).thenReturn("1 2 3");

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Invalid userId in JWT", errorBody.get("error"));

        verify(userRepository, never()).findById(any());
    }

    @Test
    void getCurrentUser_whenUserIdIsEmpty_throwsNumberFormatException() {
        // Arrange
        when(authentication.getName()).thenReturn("");

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getCurrentUser_whenLargeUserId_handlesCorrectly() {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        User largeIdUser = new User("googleXYZ", "Bob", "bob@test.com");
        largeIdUser.setId(largeId);

        when(authentication.getName()).thenReturn(String.valueOf(largeId));
        when(userRepository.findById(largeId)).thenReturn(Optional.of(largeIdUser));

        // Act
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(largeId, body.get("id"));
    }
}