package com.example.serverapp.config;

import com.example.serverapp.model.User;
import com.example.serverapp.model.User.Role;
import com.example.serverapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initUsers() {
        createUserIfNotExists("supervisor", "password123", Role.SUPERVISOR);
        createUserIfNotExists("operator", "password123", Role.OPERATOR);
    }

    private void createUserIfNotExists(String username, String password, Role role) {
        Optional<User> existingUser = userRepository.findByUsername(username);

        existingUser.ifPresentOrElse(
            user -> System.out.println("User '" + username + "' sudah ada, tidak perlu ditambahkan."),
            () -> {
                User newUser = new User(username, passwordEncoder.encode(password), role);
                userRepository.save(newUser);
                System.out.println("User '" + username + "' berhasil ditambahkan.");
            }
        );
    }
}
