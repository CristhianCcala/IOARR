package com.cristhian.IOARR.asistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    private final AsistenciaRepository asistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository, UsuarioRepository usuarioRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AsistenciaResponse marcarEntrada(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        if (asistenciaRepository.existsByUsuarioAndFecha(usuario, hoy)) {
            throw new IllegalStateException("El usuario ya registró asistencia hoy");
        }
        Asistencia asistencia = new Asistencia();
        asistencia.setUsuario(usuario);
        asistencia.setFecha(hoy);
        asistencia.setHoraEntrada(LocalTime.now());
        asistencia.setEstado(asistencia.getHoraEntrada().isAfter(HORA_LIMITE_ENTRADA)
                ? EstadoAsistencia.TARDE
                : EstadoAsistencia.PRESENTE);
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse marcarSalida(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Asistencia asistencia = asistenciaRepository.findByUsuarioAndFecha(usuario, hoy)
                .orElseThrow(() -> new IllegalStateException("No hay entrada registrada hoy"));
        if (asistencia.getHoraSalida() != null) {
            throw new IllegalStateException("El usuario ya registró salida hoy");
        }
        asistencia.setHoraSalida(LocalTime.now());
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse marcarSalidaAlmuerzo(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Asistencia asistencia = asistenciaRepository.findByUsuarioAndFecha(usuario, hoy)
                .orElseThrow(() -> new IllegalStateException("No hay entrada registrada hoy"));
        if (asistencia.getHoraSalidaAlmuerzo() != null) {
            throw new IllegalStateException("El usuario ya registró salida a almuerzo hoy");
        }
        asistencia.setHoraSalidaAlmuerzo(LocalTime.now());
        return toResponse(asistenciaRepository.save(asistencia));
    }

    @Transactional
    public AsistenciaResponse marcarRetornoAlmuerzo(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        Asistencia asistencia = asistenciaRepository.findByUsuarioAndFecha(usuario, hoy)
                .orElseThrow(() -> new IllegalStateException("No hay entrada registrada hoy"));
        if (asistencia.getHoraSalidaAlmuerzo() == null) {
            throw new IllegalStateException("No hay salida a almuerzo registrada hoy");
        }
        if (asistencia.getHoraEntradaAlmuerzo() != null) {
            throw new IllegalStateException("El usuario ya registró retorno de almuerzo hoy");
        }
        asistencia.setHoraEntradaAlmuerzo(LocalTime.now());
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
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));
        }

        List<ReporteResponse> resumen = listarParaReporte(usuarioId, desde, hasta).stream()
                .map(r -> r)
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.usuarioId() + "|" + a.usuario(),
                        this::toReporte,
                        this::acumular,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .map(ReporteDatos::toResponse)
                .toList();

        if (usuario != null && resumen.stream().noneMatch(r -> r.usuarioId().equals(usuarioId))) {
            resumen = new java.util.ArrayList<>(resumen);
            resumen.add(new ReporteResponse(usuarioId,
                    usuario.getNombre() + " " + usuario.getApellido(),
                    0, 0, 0, 0, 0, "0:00"));
        }
        return resumen;
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