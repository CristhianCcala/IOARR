package com.cristhian.IOARR.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record HorarioRequest(
        @NotNull LocalTime horaEntrada,
        @NotNull LocalTime horaSalidaAlmuerzo,
        @NotNull LocalTime horaEntradaAlmuerzo,
        @NotNull LocalTime horaSalida,
        @NotNull @PositiveOrZero Integer toleranciaMinutos,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitud,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitud) {
}