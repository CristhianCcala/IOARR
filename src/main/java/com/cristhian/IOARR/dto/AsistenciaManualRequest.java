package com.cristhian.IOARR.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cristhian.IOARR.asistencia.EstadoAsistencia;

import jakarta.validation.constraints.NotNull;

public record AsistenciaManualRequest(
        @NotNull Long usuarioId,
        @NotNull LocalDate fecha,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        LocalTime horaSalidaAlmuerzo,
        LocalTime horaEntradaAlmuerzo,
        @NotNull EstadoAsistencia estado,
        String observacion) {
}