package org.svlahov.sleepcalc.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.svlahov.sleepcalc.entity.User;
import org.svlahov.sleepcalc.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword);

        return userRepository.save(user);
    }
}
