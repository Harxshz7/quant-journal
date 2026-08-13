package com.tradingjournal.config;

import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String DEV_EMAIL = "demo@quantjournal.local";
    private static final String DEV_PASSWORD = "Demo@1234";
    private static final String DEV_FULL_NAME = "Demo Trader";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(DEV_EMAIL).isPresent()) {
            return;
        }

        User user = new User(DEV_FULL_NAME, DEV_EMAIL, passwordEncoder.encode(DEV_PASSWORD));
        userRepository.save(user);
        log.info("Seeded dev user -> {} / {}", DEV_EMAIL, DEV_PASSWORD);
    }
}
