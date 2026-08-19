package com.cristhian.IOARR.dto;

import com.cristhian.IOARR.user.Rol;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}