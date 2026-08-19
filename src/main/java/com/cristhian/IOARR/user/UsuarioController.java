package com.cristhian.IOARR.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cristhian.IOARR.dto.UsuarioMapper;
import com.cristhian.IOARR.dto.UsuarioRequest;
import com.cristhian.IOARR.dto.UsuarioResponse;
import com.cristhian.IOARR.dto.UsuarioUpdateRequest;

import jakarta.validation.Valid;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRACION', 'JEFE')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> usuarios = usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRACION', 'JEFE')")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(UsuarioMapper.toResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("El nombre de usuario ya existe"));
        }
        if (request.dni() != null && usuarioRepository.existsByDni(request.dni())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("El DNI ya está registrado"));
        }
        if (request.email() != null && usuarioRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("El email ya está registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setDni(request.dni());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol());
        usuario.setActivo(true);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioMapper.toResponse(usuarioRepository.save(usuario)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.username() != null && !request.username().equals(usuario.getUsername())
                && usuarioRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("El nombre de usuario ya existe"));
        }
        if (request.dni() != null && !request.dni().equals(usuario.getDni())
                && usuarioRepository.existsByDni(request.dni())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("El DNI ya está registrado"));
        }
        if (request.email() != null && !request.email().equals(usuario.getEmail())
                && usuarioRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("El email ya está registrado"));
        }

        if (request.username() != null) usuario.setUsername(request.username());
        if (request.password() != null) usuario.setPassword(passwordEncoder.encode(request.password()));
        if (request.nombre() != null) usuario.setNombre(request.nombre());
        if (request.apellido() != null) usuario.setApellido(request.apellido());
        if (request.dni() != null) usuario.setDni(request.dni());
        if (request.email() != null) usuario.setEmail(request.email());
        if (request.rol() != null) usuario.setRol(request.rol());

        return ResponseEntity.ok(UsuarioMapper.toResponse(usuarioRepository.save(usuario)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestBody EstadoRequest request,
                                           Authentication authentication) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        Usuario actual = (Usuario) authentication.getPrincipal();
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        if (usuario.getId().equals(actual.getId())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("No puede desactivar su propia cuenta"));
        }
        usuario.setActivo(request.activo());
        return ResponseEntity.ok(UsuarioMapper.toResponse(usuarioRepository.save(usuario)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<?> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        Usuario actual = (Usuario) authentication.getPrincipal();
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        if (usuario.getId().equals(actual.getId())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("No puede eliminar su propia cuenta"));
        }
        usuarioRepository.delete(usuario);
        return ResponseEntity.noContent().build();
    }

    private record ErrorResponse(String mensaje) {
    }

    private record EstadoRequest(boolean activo) {
    }
}