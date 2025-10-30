package com.starkindustries.config.security;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

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

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
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
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // público:
                        .requestMatchers("/login.html", "/register.html", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/all", "/auth/{id}", "/auth/me", "/auth/login").permitAll()

                        // API de sensores:
                        .requestMatchers(HttpMethod.GET,  "/api/devices/**").hasAnyRole("ADMIN","OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/api/devices/**").hasRole("ADMIN") // start/stop/cambiar periodo
                        .requestMatchers(HttpMethod.DELETE, "/api/devices/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // resto:
                        .requestMatchers("/index.html").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/auth/register", "/auth/all", "/auth/login", "/auth/{id}"
                        )
                ).addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }
}
