package com.starkindustries.config;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class BootstrapAdmin {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdmin.class);

    @Bean
    CommandLineRunner seedAdmin(UserRepo userRepo, PasswordEncoder encoder) {
        return args -> {
            // 🔍 1. Verificar si ya existe algún usuario con rol ADMIN
            boolean adminExists = userRepo.findAll().stream()
                    .anyMatch(u -> u.getRoles() != null &&
                            u.getRoles().toUpperCase().contains("ADMIN"));

            if (adminExists) {
                return;
            }

            User u = new User();
            u.setFullName("Administrador");
            u.setUsername("admin");
            u.setEmail("admin@stark.local");
            u.setPassword(encoder.encode("AdminPass123")); // cámbiala después
            u.setEnabled(true);
            u.setRoles("ADMIN");

            userRepo.save(u);
        };
    }
}
