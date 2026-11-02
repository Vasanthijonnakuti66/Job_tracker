package com.jobtracker.config;

import com.jobtracker.entity.User;
import com.jobtracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a local demo account so you can log in without registering first.
 * Password is bcrypt-hashed. If a leftover Phase 3 plaintext password exists,
 * it is upgraded on startup.
 *
 * Demo credentials are for local development only — not for production.
 */
@Component
public class DevDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    public static final String DEMO_EMAIL = "demo@jobtracker.local";
    public static final String DEMO_PASSWORD = "demo12345";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User demo = userRepository.findByEmail(DEMO_EMAIL).orElse(null);

        if (demo == null) {
            demo = userRepository.save(new User(
                    DEMO_EMAIL,
                    passwordEncoder.encode(DEMO_PASSWORD),
                    "Demo User"));
        } else if (!looksLikeBcrypt(demo.getPassword())) {
            demo.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            userRepository.save(demo);
        }

        log.info("Demo login: email={} password={} (local only)", DEMO_EMAIL, DEMO_PASSWORD);
    }

    private boolean looksLikeBcrypt(String password) {
        return password != null && (password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$"));
    }
}
