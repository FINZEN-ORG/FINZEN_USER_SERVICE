package eci.ieti.FinzenUserService.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import eci.ieti.FinzenUserService.dto.GoogleTokenDto;
import eci.ieti.FinzenUserService.dto.JwtResponseDto;
import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.security.GoogleTokenVerifier;
import eci.ieti.FinzenUserService.security.JwtTokenProvider;
import eci.ieti.FinzenUserService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    private GoogleIdToken.Payload mockPayload;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockPayload = new GoogleIdToken.Payload();
        mockPayload.setSubject("google123");
        mockPayload.setEmail("alice@test.com");
        mockPayload.set("name", "Alice Test");

        mockUser = new User("google123", "Alice Test", "alice@test.com");
        mockUser.setId(1L);
    }

    @Test
    void authenticateWithGoogle_whenValidToken_returnsJwtSuccessfully() throws Exception {
        // Arrange
        String validToken = "validGoogleToken";
        String expectedJwt = "generatedJwtToken";
        
        when(googleTokenVerifier.verify(validToken)).thenReturn(mockPayload);
        when(userService.findOrCreateUser("google123", "Alice Test", "alice@test.com")).thenReturn(mockUser);
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(expectedJwt);

        GoogleTokenDto dto = new GoogleTokenDto(validToken);

        // Act
        ResponseEntity<?> response = authController.authenticateWithGoogle(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof JwtResponseDto);
        
        JwtResponseDto jwtResponse = (JwtResponseDto) response.getBody();
        assertEquals(expectedJwt, jwtResponse.getAccessToken());

        verify(googleTokenVerifier, times(1)).verify(validToken);
        verify(userService, times(1)).findOrCreateUser("google123", "Alice Test", "alice@test.com");
        verify(jwtTokenProvider, times(1)).generateToken(mockUser);
    }

    @Test
    void authenticateWithGoogle_whenTokenIsNull_returns401() throws Exception {
        // Arrange
        String invalidToken = "invalidToken";
        when(googleTokenVerifier.verify(invalidToken)).thenReturn(null);

        GoogleTokenDto dto = new GoogleTokenDto(invalidToken);

        // Act
        ResponseEntity<?> response = authController.authenticateWithGoogle(dto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Invalid Google ID Token", errorBody.get("error"));

        verify(googleTokenVerifier, times(1)).verify(invalidToken);
        verify(userService, never()).findOrCreateUser(any(), any(), any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void authenticateWithGoogle_whenGeneralSecurityExceptionThrown_returns500() throws Exception {
        // Arrange
        String token = "securityErrorToken";
        when(googleTokenVerifier.verify(token))
                .thenThrow(new GeneralSecurityException("Security error"));

        GoogleTokenDto dto = new GoogleTokenDto(token);

        // Act
        ResponseEntity<?> response = authController.authenticateWithGoogle(dto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Failed to verify Google ID Token", errorBody.get("error"));
        assertTrue(errorBody.containsKey("details"));

        verify(userService, never()).findOrCreateUser(any(), any(), any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void authenticateWithGoogle_whenIOExceptionThrown_returns500() throws Exception {
        // Arrange
        String token = "ioErrorToken";
        when(googleTokenVerifier.verify(token))
                .thenThrow(new IOException("Network error"));

        GoogleTokenDto dto = new GoogleTokenDto(token);

        // Act
        ResponseEntity<?> response = authController.authenticateWithGoogle(dto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Failed to verify Google ID Token", errorBody.get("error"));
        assertEquals("Network error", errorBody.get("details"));

        verify(userService, never()).findOrCreateUser(any(), any(), any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void authenticateWithGoogle_whenPayloadHasAllRequiredFields_extractsCorrectly() throws Exception {
        // Arrange
        String token = "completeToken";
        GoogleIdToken.Payload completePayload = new GoogleIdToken.Payload();
        completePayload.setSubject("googleId789");
        completePayload.setEmail("complete@test.com");
        completePayload.set("name", "Complete User");

        User user = new User("googleId789", "Complete User", "complete@test.com");
        user.setId(2L);

        when(googleTokenVerifier.verify(token)).thenReturn(completePayload);
        when(userService.findOrCreateUser("googleId789", "Complete User", "complete@test.com")).thenReturn(user);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwtToken");

        GoogleTokenDto dto = new GoogleTokenDto(token);

        // Act
        ResponseEntity<?> response = authController.authenticateWithGoogle(dto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).findOrCreateUser("googleId789", "Complete User", "complete@test.com");
    }
}