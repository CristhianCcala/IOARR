package com.cristhian.IOARR.dto;

import com.cristhian.IOARR.user.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 6) String password,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Size(max = 80) String apellido,
        @Size(max = 20) String dni,
        @Size(max = 100) String email,
        @NotNull Rol rol) {
}