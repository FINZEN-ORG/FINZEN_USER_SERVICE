package eci.ieti.FinzenUserService.service;

import eci.ieti.FinzenUserService.model.User;
import eci.ieti.FinzenUserService.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findOrCreateUser(String googleId, String name, String email) {
        return userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> userRepository.save(new User(googleId, name, email)));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}