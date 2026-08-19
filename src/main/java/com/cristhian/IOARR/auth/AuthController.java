package com.cristhian.IOARR.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cristhian.IOARR.dto.LoginRequest;
import com.cristhian.IOARR.dto.LoginResponse;
import com.cristhian.IOARR.dto.UsuarioMapper;
import com.cristhian.IOARR.dto.UsuarioResponse;
import com.cristhian.IOARR.security.JwtService;
import com.cristhian.IOARR.user.Usuario;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            Usuario usuario = (Usuario) authentication.getPrincipal();
            String token = jwtService.generarToken(usuario);
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    "Bearer",
                    UsuarioMapper.toResponse(usuario),
                    usuario.getRol()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Credenciales inválidas"));
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> perfil(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(UsuarioMapper.toResponse(usuario));
    }

    private record ErrorResponse(String mensaje) {
    }
}