package edu.bu.cs411.group10.curre.user;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(UserDTO dto) {
        if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        User saved = userRepository.save(user);

        return new AuthResponse(saved.getId(), saved.getEmail(), "Registration successful");
    }

    public AuthResponse login(UserDTO dto) {
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = existing.get();
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return new AuthResponse(user.getId(), user.getEmail(), "Login successful");
    }
}
