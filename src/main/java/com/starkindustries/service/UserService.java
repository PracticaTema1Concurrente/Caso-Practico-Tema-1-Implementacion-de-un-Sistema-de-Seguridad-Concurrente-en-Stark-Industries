package com.starkindustries.service;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(User newUser) {
        if (userRepo.existsByEmail(newUser.getEmail()))
            throw new IllegalArgumentException("El email ya está registrado");
        if (userRepo.existsByUsername(newUser.getUsername()))
            throw new IllegalArgumentException("El usuario ya existe");

        User u = new User();
        u.setFullName(newUser.getFullName().trim());
        u.setUsername(newUser.getUsername().trim());
        u.setEmail(newUser.getEmail().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(newUser.getPassword()));
        u.setEnabled(true);
        u.setRoles("BASIC");
        return userRepo.save(u);
    }

    public List<User> listAll() {
        return userRepo.findAll();
    }

    public User getById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("No existe el usuario"));
    }

    public User getByEmail(String email) {
        return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("No existe el usuario"));
    }

    public void delete(Long id) {
        userRepo.deleteById(id);
    }
}
