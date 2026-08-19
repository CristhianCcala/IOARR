package com.cristhian.IOARR.dto;

public record ReporteResponse(
        Long usuarioId,
        String usuario,
        long totalRegistros,
        long presentes,
        long tardes,
        long ausentes,
        long diasFaltados,
        String tiempoAcumuladoTarde) {
}