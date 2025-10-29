package com.starkindustries.web;

import com.starkindustries.domain.User;
import com.starkindustries.domain.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepo users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepo users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/auth/register")
    public String register(
            @RequestParam("nombre_apellidos") String nombreApellidos,
            @RequestParam("usuario") String usuario,
            @RequestParam("correo") String correo,
            @RequestParam("password") String rawPassword,
            RedirectAttributes ra
    ) {
        try {
            // ¿Existe ya usuario o correo?
            if (users.existsByUsuarioOrCorreo(usuario, correo)) {
                ra.addAttribute("exists", "");
                return "redirect:/register.html";
            }

            // Crear entidad y guardar
            User u = new User();
            u.setNombreApellidos(nombreApellidos);
            u.setUsuario(usuario);
            u.setCorreo(correo);
            u.setPassword(encoder.encode(rawPassword)); // ¡importante!
            u.setAuthority("BASICO");
            u.setEnabled(true);

            users.save(u);

            ra.addAttribute("ok", "");
            return "redirect:/register.html";

        } catch (Exception e) {
            e.printStackTrace();
            ra.addAttribute("error", "");
            return "redirect:/register.html";
        }
    }
}
