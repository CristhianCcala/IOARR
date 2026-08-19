package com.cristhian.IOARR.dto;

import java.time.LocalTime;

public record HorarioResponse(
        Long id,
        LocalTime horaEntrada,
        LocalTime horaSalidaAlmuerzo,
        LocalTime horaEntradaAlmuerzo,
        LocalTime horaSalida,
        int toleranciaMinutos,
        double latitud,
        double longitud) {
}