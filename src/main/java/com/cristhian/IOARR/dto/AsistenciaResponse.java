package com.cristhian.IOARR.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.cristhian.IOARR.asistencia.EstadoAsistencia;

public record AsistenciaResponse(
        Long id,
        Long usuarioId,
        String usuario,
        LocalDate fecha,
        LocalTime horaEntrada,
        LocalTime horaSalida,
        LocalTime horaSalidaAlmuerzo,
        LocalTime horaEntradaAlmuerzo,
        EstadoAsistencia estado,
        String observacion) {
}