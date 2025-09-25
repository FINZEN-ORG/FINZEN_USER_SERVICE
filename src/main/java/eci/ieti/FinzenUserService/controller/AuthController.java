package eci.ieti.FinzenUserService.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import eci.ieti.FinzenUserService.dto.GoogleTokenDto;
import eci.ieti.FinzenUserService.dto.JwtResponseDto;
import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.security.GoogleTokenVerifier;
import eci.ieti.FinzenUserService.security.JwtTokenProvider;
import eci.ieti.FinzenUserService.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, GoogleTokenVerifier googleTokenVerifier, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody GoogleTokenDto googleTokenDto) {
        Optional<GoogleIdToken.Payload> payloadOptional = googleTokenVerifier.verify(googleTokenDto.getIdToken());

        if (payloadOptional.isPresent()) {
            GoogleIdToken.Payload payload = payloadOptional.get();

            // Extraemos los datos del usuario
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Buscamos o creamos el usuario en nuestra base de datos
            User user = userService.findOrCreateUser(googleId, name, email);

            // Generamos nuestro propio token JWT de FinZen
            String jwt = jwtTokenProvider.generateToken(user);

            // Devolvemos el token JWT al cliente
            return ResponseEntity.ok(new JwtResponseDto(jwt));
        } else {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid Google ID Token"));
        }
    }
}