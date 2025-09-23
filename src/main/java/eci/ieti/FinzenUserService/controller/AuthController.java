package eci.ieti.FinzenUserService.controller;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> payload) {
        String idToken = payload.get("idToken");

        // Placeholder response
        User user = userService.findOrCreateUser("mockGoogleId", "Mock User", "mock.user@example.com");

        Map<String, String> response = new HashMap<>();
        response.put("token", "placeholder-jwt-for-" + user.getName());
        return ResponseEntity.ok(response);
    }
}