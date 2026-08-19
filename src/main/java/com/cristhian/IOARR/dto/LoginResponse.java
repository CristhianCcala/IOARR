package com.cristhian.IOARR.dto;

import com.cristhian.IOARR.user.Rol;

public record LoginResponse(
        String token,
        String tipo,
        UsuarioResponse usuario,
        Rol rol) {
}