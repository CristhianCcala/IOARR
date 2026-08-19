package com.cristhian.IOARR.dto;

import com.cristhian.IOARR.user.Rol;

public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        String apellido,
        String dni,
        String email,
        Rol rol,
        boolean activo) {
}