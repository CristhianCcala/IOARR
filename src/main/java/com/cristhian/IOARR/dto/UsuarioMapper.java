package com.cristhian.IOARR.dto;

import com.cristhian.IOARR.user.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.isActivo());
    }
}