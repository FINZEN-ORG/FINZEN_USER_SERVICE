package eci.ieti.FinzenUserService.controller;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            Long userId = Long.valueOf(authentication.getName());

            Optional<User> user = userRepository.findById(userId);

            return user.map(u -> ResponseEntity.ok(
                    Map.of(
                            "id", u.getId(),
                            "name", u.getName(),
                            "email", u.getEmail()
                    )
            )).orElseGet(() -> ResponseEntity.notFound().build());

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId in JWT"));
        }
    }
}