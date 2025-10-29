package com.starkindustries.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_usuario", columnNames = "usuario"),
                @UniqueConstraint(name = "uk_users_correo", columnNames = "correo")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Size(max = 100)
    @Column(name = "nombre_apellidos")
    private String nombreApellidos;

    @NotBlank
    @Size(max = 50)
    @Column(name = "usuario", nullable = false, length = 50)
    private String usuario;

    @NotBlank
    @Email
    @Size(max = 80)
    @Column(name = "correo", nullable = false, length = 80)
    private String correo;

    @NotBlank
    @Size(max = 100)
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @NotBlank
    @Size(max = 50)
    @Column(name = "authority", nullable = false, length = 50)
    private String authority;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    // Getters y setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombreApellidos() { return nombreApellidos; }
    public void setNombreApellidos(String nombreApellidos) { this.nombreApellidos = nombreApellidos; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
