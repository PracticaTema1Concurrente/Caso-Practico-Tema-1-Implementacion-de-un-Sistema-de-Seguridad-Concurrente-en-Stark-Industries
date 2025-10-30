package com.starkindustries.web;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import com.starkindustries.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService,
                          UserRepo userRepo,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    // ====== API JSON (sin cambios) ======
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

    @PostMapping(value="/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String,Object>> registerJson(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password
    ){
        try {
            // (Opcional) Validaciones simples
            if (fullName == null || fullName.trim().length() < 3)
                return ResponseEntity.badRequest().body(Map.of("ok", false, "field", "fullName", "error", "Nombre demasiado corto"));
            if (username == null || username.trim().length() < 3)
                return ResponseEntity.badRequest().body(Map.of("ok", false, "field", "username", "error", "Usuario demasiado corto"));
            if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$"))
                return ResponseEntity.badRequest().body(Map.of("ok", false, "field", "email", "error", "Email no válido"));
            if (password == null || password.length() < 8)
                return ResponseEntity.badRequest().body(Map.of("ok", false, "field", "password", "error", "Mínimo 8 caracteres"));

            User u = new User();
            u.setFullName(fullName.trim());
            u.setUsername(username.trim());
            u.setEmail(email.trim());
            u.setPassword(password);

            userService.register(u); // aquí ya encripta

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Usuario registrado correctamente",
                    "username", u.getUsername(),
                    "email", u.getEmail()
            ));
        } catch (IllegalArgumentException ex) {
            // Tu servicio lanza esto para duplicados u otras reglas de negocio
            String field = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email") ? "email" :
                    ex.getMessage() != null && ex.getMessage().toLowerCase().contains("usuario") ? "username" : null;
            return ResponseEntity.status(409).body(Map.of(
                    "ok", false,
                    "error", ex.getMessage(),
                    "field", field
            ));
        } catch (Exception ex) {
            log.error("Error registrando usuario", ex);
            return ResponseEntity.status(500).body(Map.of(
                    "ok", false,
                    "error", "Error interno al registrar"
            ));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<Integer> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        try {
            var authReq = new UsernamePasswordAuthenticationToken(username, password);
            var authentication = authenticationManager.authenticate(authReq);

            // Guardar en SecurityContext + sesión
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            // OK => 0
            return ResponseEntity.ok(0);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            // KO => -1
            return ResponseEntity.ok(-1);
        }
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> getAllUsers() {
        List<User> users = userService.listAll(); // usa tu servicio que ya los obtiene
        log.info("DEBUG /auth/all -> {} usuarios", users.size());

        return Map.of(
                "ok", true,
                "count", users.size(),
                "users", users
        );
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> me (Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("ok", false));
        }
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "username", authentication.getName(),
                "roles", roles
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value="/{id}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String,Object>> setRoles(
            @PathVariable Long id,
            @RequestParam String roles // p.ej. "OPERATOR" o "ADMIN,OPERATOR"
    ){
        User u = userService.updateRoles(id, roles);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", u.getId(),
                "roles", u.getRoles()
        ));
    }


}
