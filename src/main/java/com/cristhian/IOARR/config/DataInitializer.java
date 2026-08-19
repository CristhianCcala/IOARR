package com.cristhian.IOARR.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cristhian.IOARR.user.Rol;
import com.cristhian.IOARR.user.Usuario;
import com.cristhian.IOARR.user.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!usuarioRepository.existsByUsername("admin")) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setRol(Rol.ADMINISTRACION);
                admin.setActivo(true);
                usuarioRepository.save(admin);
            }
        };
    }
}