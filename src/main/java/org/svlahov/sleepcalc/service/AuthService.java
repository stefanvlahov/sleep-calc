package org.svlahov.sleepcalc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.svlahov.sleepcalc.config.JwtService;
import org.svlahov.sleepcalc.entity.User;
import org.svlahov.sleepcalc.repository.UserRepository;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword);

        return userRepository.save(user);
    }

    public void initiatePasswordReset(String username) {
        if (userRepository.findByUsername(username).isEmpty()) {
            logger.info("Password reset requested for unknown username: {}", username);
            return;
        }
        String token = jwtService.generatePasswordResetToken(username);
        logger.info("Password reset link: http://localhost:5173/reset-password?token={}", token);
    }

    public void resetPassword(String token, String newPassword) {
        String username = jwtService.validatePasswordResetToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
