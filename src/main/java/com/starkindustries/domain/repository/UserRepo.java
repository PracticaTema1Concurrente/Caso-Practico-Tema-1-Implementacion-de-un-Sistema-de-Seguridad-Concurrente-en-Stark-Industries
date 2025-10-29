package com.starkindustries.domain.repository;

import com.starkindustries.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByUsuario(String usuario);
    Optional<User> findByCorreo(String correo);

    boolean existsByUsuarioOrCorreo(String usuario, String correo);
}
