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

    /**
     * Busca un usuario por su email. Si no existe, lo crea.
     * @param googleId El ID único proporcionado por Google.
     * @param name El nombre del usuario proporcionado por Google.
     * @param email El email del usuario proporcionado por Google.
     * @return El usuario existente o el nuevo usuario creado.
     */
    public User findOrCreateUser(String googleId, String name, String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        return existingUser.orElseGet(() -> {
            User newUser = new User(googleId, name, email);
            return userRepository.save(newUser);
        });
    }
}