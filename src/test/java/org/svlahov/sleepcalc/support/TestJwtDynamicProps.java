package org.svlahov.sleepcalc.support;

import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Base test class that injects a fresh, random JWT signing key for each test run.
 * This avoids committing any secret-like value to the repository while keeping
 * Spring Boot tests hermetic and reproducible.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestJwtDynamicProps {

    private static final String JWT_SECRET_PROPERTY = "jwt.secret.key";

    @DynamicPropertySource
    static void registerJwtProps(DynamicPropertyRegistry registry) {
        // Generate a 32-byte (256-bit) random key and provide it Base64-encoded
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        String b64 = Base64.getEncoder().encodeToString(keyBytes);
        registry.add(JWT_SECRET_PROPERTY, () -> b64);

        // Keep expiration aligned with test expectations
        registry.add("jwt.secret.expiration", () -> "3600000");
    }
}
