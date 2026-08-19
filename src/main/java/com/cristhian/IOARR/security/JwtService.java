package com.cristhian.IOARR.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.cristhian.IOARR.user.Usuario;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre() + " " + usuario.getApellido())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extraerUsuario(String token) {
        return obtenerClaims(token).getSubject();
    }

    public boolean esValido(String token, UserDetails userDetails) {
        String username = extraerUsuario(token);
        return username.equals(userDetails.getUsername()) && !obtenerClaims(token).getExpiration().before(new Date());
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}