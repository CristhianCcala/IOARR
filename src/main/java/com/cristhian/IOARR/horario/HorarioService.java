package com.cristhian.IOARR.horario;

import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.cristhian.IOARR.dto.HorarioRequest;
import com.cristhian.IOARR.dto.HorarioResponse;

@Service
public class HorarioService {

    public static final LocalTime HORA_ENTRADA_DEFECTO = LocalTime.of(8, 0);
    public static final LocalTime HORA_SALIDA_ALMUERZO_DEFECTO = LocalTime.of(13, 0);
    public static final LocalTime HORA_ENTRADA_ALMUERZO_DEFECTO = LocalTime.of(14, 30);
    public static final LocalTime HORA_SALIDA_DEFECTO = LocalTime.of(17, 30);
    public static final int TOLERANCIA_DEFECTO = 10;
    public static final double LATITUD_DEFECTO = -14.118589819492668;
    public static final double LONGITUD_DEFECTO = -72.24576119540694;

    private final HorarioRepository horarioRepository;

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    public HorarioResponse obtener() {
        return toResponse(obtenerOCrear());
    }

    public HorarioResponse actualizar(HorarioRequest request) {
        Horario horario = obtenerOCrear();
        horario.setHoraEntrada(request.horaEntrada());
        horario.setHoraSalidaAlmuerzo(request.horaSalidaAlmuerzo());
        horario.setHoraEntradaAlmuerzo(request.horaEntradaAlmuerzo());
        horario.setHoraSalida(request.horaSalida());
        horario.setToleranciaMinutos(request.toleranciaMinutos());
        horario.setLatitud(request.latitud());
        horario.setLongitud(request.longitud());
        return toResponse(horarioRepository.save(horario));
    }

    private Horario obtenerOCrear() {
        return horarioRepository.findAll().stream().findFirst()
                .orElseGet(this::crearPorDefecto);
    }

    private Horario crearPorDefecto() {
        Horario horario = new Horario();
        horario.setHoraEntrada(HORA_ENTRADA_DEFECTO);
        horario.setHoraSalidaAlmuerzo(HORA_SALIDA_ALMUERZO_DEFECTO);
        horario.setHoraEntradaAlmuerzo(HORA_ENTRADA_ALMUERZO_DEFECTO);
        horario.setHoraSalida(HORA_SALIDA_DEFECTO);
        horario.setToleranciaMinutos(TOLERANCIA_DEFECTO);
        horario.setLatitud(LATITUD_DEFECTO);
        horario.setLongitud(LONGITUD_DEFECTO);
        return horarioRepository.save(horario);
    }

    private HorarioResponse toResponse(Horario h) {
        return new HorarioResponse(
                h.getId(),
                h.getHoraEntrada(),
                h.getHoraSalidaAlmuerzo(),
                h.getHoraEntradaAlmuerzo(),
                h.getHoraSalida(),
                h.getToleranciaMinutos(),
                h.getLatitud(),
                h.getLongitud());
    }
}