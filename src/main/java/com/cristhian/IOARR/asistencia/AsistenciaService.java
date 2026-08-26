package com.cristhian.IOARR.asistencia;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cristhian.IOARR.dto.AsistenciaResponse;
import com.cristhian.IOARR.dto.AsistenciaManualRequest;
import com.cristhian.IOARR.dto.AsistenciaUpdateRequest;
import com.cristhian.IOARR.dto.ReporteResponse;
import com.cristhian.IOARR.user.Usuario;
import com.cristhian.IOARR.user.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class AsistenciaService {

    public static final LocalTime HORA_LIMITE_ENTRADA = LocalTime.of(8, 0);

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final Clock CLOCK_PERU = Clock.system(ZONA_PERU);

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository, UsuarioRepository usuarioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AsistenciaResponse marcarEntrada(Usuario usuario) {
        LocalDate hoy = LocalDate.now(CLOCK_PERU);
        if (asistenciaRepository.existsByUsuarioAndFecha(usuario, hoy)) {
            throw new IllegalStateException("El usuario ya registró asistencia hoy");
        }
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(hoy);
        asistencia.setHoraEntrada(LocalTime.now(CLOCK_PERU));
        asistencia.setEstado(asistencia.getHoraEntrada().isAfter(HORA_LIMITE_ENTRADA)
                ? EstadoAsistencia.TARDE
                : EstadoAsistencia.PRESENTE);
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse marcarSalida(Usuario usuario) {
        LocalDate hoy = LocalDate.now(CLOCK_PERU);
        Asistencia asistencia = asistenciaRepository.findByUsuarioAndFecha(usuario, hoy)
                .orElseThrow(() -> new IllegalStateException("No hay entrada registrada hoy"));
        if (asistencia.getHoraSalida() != null) {
            throw new IllegalStateException("El usuario ya registró salida hoy");
        }
        asistencia.setHoraSalida(LocalTime.now(CLOCK_PERU));
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse marcarSalidaAlmuerzo(Usuario usuario) {
        LocalDate hoy = LocalDate.now(CLOCK_PERU);
        Asistencia asistencia = asistenciaRepository.findByUsuarioAndFecha(usuario, hoy)
                .orElseThrow(() -> new IllegalStateException("No hay entrada registrada hoy"));
        if (asistencia.getHoraSalidaAlmuerzo() != null) {
            throw new IllegalStateException("El usuario ya registró salida a almuerzo hoy");
        }
        asistencia.setHoraSalidaAlmuerzo(LocalTime.now(CLOCK_PERU));
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse marcarRetornoAlmuerzo(Usuario usuario) {
        LocalDate hoy = LocalDate.now(CLOCK_PERU);
        Asistencia asistencia = asistenciaRepository.findByUsuarioAndFecha(usuario, hoy)
                .orElseThrow(() -> new IllegalStateException("No hay entrada registrada hoy"));
        if (asistencia.getHoraSalidaAlmuerzo() == null) {
            throw new IllegalStateException("No hay salida a almuerzo registrada hoy");
        }
        if (asistencia.getHoraEntradaAlmuerzo() != null) {
            throw new IllegalStateException("El usuario ya registró retorno de almuerzo hoy");
        }
        asistencia.setHoraEntradaAlmuerzo(LocalTime.now(CLOCK_PERU));
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> misAsistencias(Usuario usuario, EstadoAsistencia estado) {
        Specification<Asistencia> spec = (root, query, cb) -> {
            Predicate p = cb.equal(root.get("usuario"), usuario);
            if (estado != null) {
                p = cb.and(p, cb.equal(root.get("estado"), estado));
            }
            query.orderBy(cb.desc(root.get("fecha")));
            return p;
        };
        return asistenciaRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> listar(EstadoAsistencia estado) {
        Specification<Asistencia> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (estado != null) {
                p = cb.and(p, cb.equal(root.get("estado"), estado));
            }
            query.orderBy(cb.desc(root.get("fecha")));
            return p;
        };
        return asistenciaRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
    }

    private List<AsistenciaResponse> listarParaReporte(Long usuarioId, LocalDate desde, LocalDate hasta) {
        Specification<Asistencia> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (usuarioId != null) {
                Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
                if (usuario != null) {
                    p = cb.and(p, cb.equal(root.get("usuario"), usuario));
                }
            }
            if (desde != null && hasta != null) {
                p = cb.and(p, cb.between(root.get("fecha"), desde, hasta));
            } else if (desde != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            } else if (hasta != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }
            return p;
        };
        return asistenciaRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AsistenciaResponse obtener(Long id) {
        return toResponse(asistenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asistencia no encontrada: " + id)));
    }

    @Transactional
    public AsistenciaResponse crearManual(AsistenciaManualRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + request.usuarioId()));
        if (asistenciaRepository.existsByUsuarioAndFecha(usuario, request.fecha())) {
            throw new IllegalStateException("El usuario ya tiene un registro para esa fecha");
        }
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(request.fecha());
        asistencia.setHoraEntrada(request.horaEntrada());
        asistencia.setHoraSalida(request.horaSalida());
        asistencia.setHoraSalidaAlmuerzo(request.horaSalidaAlmuerzo());
        asistencia.setHoraEntradaAlmuerzo(request.horaEntradaAlmuerzo());
        asistencia.setEstado(request.estado());
        asistencia.setObservacion(request.observacion());
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse actualizar(Long id, AsistenciaUpdateRequest request) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asistencia no encontrada: " + id));
        if (request.fecha() != null) {
            Usuario usuario = asistencia.getUsuario();
            LocalDate nuevaFecha = request.fecha();
            Optional<Asistencia> conflicto = asistenciaRepository.findByUsuarioAndFecha(usuario, nuevaFecha);
            if (conflicto.isPresent() && !conflicto.get().getId().equals(id)) {
                throw new IllegalStateException("El usuario ya tiene un registro para esa fecha");
            }
            asistencia.setFecha(nuevaFecha);
        }
        if (request.horaEntrada() != null) asistencia.setHoraEntrada(request.horaEntrada());
        if (request.horaSalida() != null) asistencia.setHoraSalida(request.horaSalida());
        if (request.horaSalidaAlmuerzo() != null) asistencia.setHoraSalidaAlmuerzo(request.horaSalidaAlmuerzo());
        if (request.horaEntradaAlmuerzo() != null) asistencia.setHoraEntradaAlmuerzo(request.horaEntradaAlmuerzo());
        if (request.estado() != null) asistencia.setEstado(request.estado());
        if (request.observacion() != null) asistencia.setObservacion(request.observacion());
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new IllegalArgumentException("Asistencia no encontrada: " + id);
        }
        asistenciaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> reporte(Long usuarioId, LocalDate desde, LocalDate hasta) {
        List<Usuario> usuarios;
        if (usuarioId != null) {
            Usuario u = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
            usuarios = List.of(u);
        } else {
            usuarios = usuarioRepository.findAll();
        }

        LocalDate fechaFin = hasta != null ? hasta : LocalDate.now(CLOCK_PERU);

        List<AsistenciaResponse> asistencias = listarParaReporte(usuarioId, desde, fechaFin);

        Map<Long, List<AsistenciaResponse>> asistenciasPorUsuario = asistencias.stream()
                .collect(Collectors.groupingBy(AsistenciaResponse::usuarioId));

        List<ReporteResponse> resumen = new ArrayList<>();

        for (Usuario u : usuarios) {
            List<AsistenciaResponse> userAsistencias = asistenciasPorUsuario.getOrDefault(u.getId(), List.of());

            long total = userAsistencias.size();
            long presentes = 0;
            long tardes = 0;
            long ausentes = 0;
            long diasFaltados = 0;
            long minutosTarde = 0;

            for (AsistenciaResponse a : userAsistencias) {
                switch (a.estado()) {
                    case PRESENTE -> presentes++;
                    case TARDE -> {
                        tardes++;
                        minutosTarde += minutosTarde(a);
                    }
                    case AUSENTE -> ausentes++;
                    case PERMISO, JUSTIFICADO, VACACIONES -> {}
                }
            }

            long diasFaltadosCalculados;
            if (!userAsistencias.isEmpty()) {
                LocalDate primeraFecha = userAsistencias.stream()
                        .map(AsistenciaResponse::fecha)
                        .min(LocalDate::compareTo)
                        .orElse(fechaFin);

                LocalDate fechaInicioCalculo = (desde != null && desde.isAfter(primeraFecha)) ? desde : primeraFecha;

                long diasLaborables = calcularDiasLaborables(fechaInicioCalculo, fechaFin);
                long diasConRegistro = presentes + tardes + ausentes;
                diasFaltadosCalculados = diasLaborables - diasConRegistro;
                if (diasFaltadosCalculados < 0) diasFaltadosCalculados = 0;
            } else {
                diasFaltadosCalculados = 0;
            }

            resumen.add(new ReporteResponse(u.getId(),
                    u.getNombre() + " " + u.getApellido(),
                    total, presentes, tardes, ausentes,
                    diasFaltadosCalculados, formatoHHMM(minutosTarde)));
        }

        return resumen;
    }

    private long calcularDiasLaborables(LocalDate desde, LocalDate hasta) {
        long count = 0;
        LocalDate fecha = desde;
        while (!fecha.isAfter(hasta)) {
            DayOfWeek dow = fecha.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            fecha = fecha.plusDays(1);
        }
        return count;
    }

    private ReporteDatos toReporte(AsistenciaResponse r) {
        ReporteDatos d = new ReporteDatos();
        d.usuarioId = r.usuarioId();
        d.usuario = r.usuario();
        d.total = 1;
        incrementarEstado(d, r.estado());
        d.minutosTarde += minutosTarde(r);
        return d;
    }

    private ReporteDatos acumular(ReporteDatos a, ReporteDatos b) {
        a.total += b.total;
        a.presentes += b.presentes;
        a.tardes += b.tardes;
        a.ausentes += b.ausentes;
        a.diasFaltados += b.diasFaltados;
        a.minutosTarde += b.minutosTarde;
        return a;
    }

    private long minutosTarde(AsistenciaResponse r) {
        if (r.estado() != EstadoAsistencia.TARDE || r.horaEntrada() == null) {
            return 0;
        }
        long tarde = java.time.Duration.between(HORA_LIMITE_ENTRADA, r.horaEntrada()).toMinutes();
        return Math.max(0, tarde);
    }

    private void incrementarEstado(ReporteDatos d, EstadoAsistencia estado) {
        switch (estado) {
            case PRESENTE -> d.presentes++;
            case TARDE -> d.tardes++;
            case AUSENTE -> {
                d.ausentes++;
                d.diasFaltados++;
            }
            case PERMISO, JUSTIFICADO, VACACIONES -> {
            }
        }
    }

    private static class ReporteDatos {
        Long usuarioId;
        String usuario;
        long total;
        long presentes;
        long tardes;
        long ausentes;
        long diasFaltados;
        long minutosTarde;

        ReporteResponse toResponse() {
            return new ReporteResponse(usuarioId, usuario, total, presentes, tardes, ausentes,
                    diasFaltados, formatoHHMM(minutosTarde));
        }
    }

    static String formatoHHMM(long minutos) {
        long horas = minutos / 60;
        long min = minutos % 60;
        return horas + ":" + (min < 10 ? "0" : "") + min;
    }

    private AsistenciaResponse toResponse(Asistencia a) {
        return new AsistenciaResponse(
                a.getId(),
                a.getUsuario().getId(),
                a.getUsuario().getNombre() + " " + a.getUsuario().getApellido(),
                a.getFecha(),
                a.getHoraEntrada(),
                a.getHoraSalida(),
                a.getHoraSalidaAlmuerzo(),
                a.getHoraEntradaAlmuerzo(),
                a.getEstado(),
                a.getObservacion());
    }
}