package com.starkindustries.config.security;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cargamos usuarios desde tu propia tabla (UserRepo)
    @Bean
    UserDetailsService userDetailsService(UserRepo userRepo) {
        return username -> {
            // Permite login por username o por email
            User u = userRepo.findByUsername(username)
                    .or(() -> userRepo.findByEmail(username))
                    .orElseThrow(() -> new UsernameNotFoundException("No existe usuario: " + username));

            var authorities = Arrays.stream(u.getRoles().split(","))
                    .map(String::trim)
                    .filter(r -> !r.isEmpty())
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UserDetails details = org.springframework.security.core.userdetails.User
                    .withUsername(u.getUsername())
                    .password(u.getPassword())
                    .authorities(authorities)
                    .accountLocked(false)
                    .disabled(!u.isEnabled())
                    .build();
            return details;
        };
    }

    @Bean
    DaoAuthenticationProvider authProvider(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login.html", "/register.html",
                                "/css/**", "/js/**", "/images/**",
                                "/auth/register" // permitir el POST de registro
                        ).permitAll()
                        .requestMatchers("/index.html").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")          // página de login
                        .loginProcessingUrl("/auth/login") // endpoint del form
                        .defaultSuccessUrl("/index.html", true) // a dónde ir al loguear
                        .failureUrl("/login.html?error")   // feedback de error
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/auth/logout") // opcional
                );

        return http.build();
    }
}
