package com.cristhian.IOARR.dto;

import com.cristhian.IOARR.user.Rol;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @Size(min = 3, max = 50) String username,
        @Size(min = 6) String password,
        @Size(max = 80) String nombre,
        @Size(max = 80) String apellido,
        @Size(max = 20) String dni,
        @Size(max = 100) String email,
        Rol rol) {
}