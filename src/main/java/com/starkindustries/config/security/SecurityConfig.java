package com.starkindustries.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Usuarios desde BBDD
    @Bean
    UserDetailsService users(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // estáticos y páginas públicas
                        .requestMatchers("/login.html", "/register.html", "/schema.sql", "/css/**", "/js/**", "/fonts/**").permitAll()
                        // permitir el POST del registro
                        .requestMatchers("/auth/register").permitAll()
                        // lecturas públicas (si lo deseas)
                        .requestMatchers("/api/sensors/readings/**").permitAll()
                        // resto de la API requiere auth
                        .requestMatchers("/api/**").authenticated()
                        // cualquier otra ruta requiere auth
                        .anyRequest().authenticated()
                )
                // Login por formulario (alineado con tu <form action="/auth/login">)
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login.html?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .permitAll()
                )
                // Para la API, que responda 401 en vez de redirigir a /login.html
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                // Si tu HTML es estático, puedes desactivar CSRF globalmente;
                // si prefieres granular, ignora solo API y registro:
                .csrf(csrf -> csrf.disable());
        // .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/auth/register", "/auth/login"));

        return http.build();
    }
}
