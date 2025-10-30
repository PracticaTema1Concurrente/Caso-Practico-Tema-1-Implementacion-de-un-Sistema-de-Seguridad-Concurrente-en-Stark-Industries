package com.starkindustries.web;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import com.starkindustries.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepo userRepo;

    public AuthController(UserService userService, UserRepo userRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
    }

    // ====== API JSON ======
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> create(@Valid @RequestBody User body) {
        User u = userService.register(body);
        return ResponseEntity.created(URI.create("/auth/" + u.getId()))
                .body(Map.of(
                        "id", u.getId(),
                        "fullName", u.getFullName(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "enabled", u.isEnabled(),
                        "roles", u.getRoles()
                ));
    }

    // ====== FORM register.html ======
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String registerForm(@Valid @ModelAttribute User formUser, RedirectAttributes ra) {
        try {
            userService.register(formUser);
            return "redirect:/login.html?registered=1";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("registerError", ex.getMessage());
            return "redirect:/register.html?error=1";
        }
    }


    // ====== Lectura usuarios (para pruebas/admin) ======
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<User> list() { return userService.listAll(); }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User getUser(@PathVariable Long id) { return userService.getById(id); }

    @GetMapping(value = "/by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User getByEmail(@RequestParam String email) { return userService.getByEmail(email); }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
