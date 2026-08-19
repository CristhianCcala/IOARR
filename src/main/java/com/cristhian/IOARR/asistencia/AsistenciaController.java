package com.cristhian.IOARR.asistencia;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cristhian.IOARR.dto.AsistenciaManualRequest;
import com.cristhian.IOARR.dto.AsistenciaResponse;
import com.cristhian.IOARR.dto.AsistenciaUpdateRequest;
import com.cristhian.IOARR.dto.ReporteResponse;
import com.cristhian.IOARR.user.Usuario;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/entrada")
    public ResponseEntity<AsistenciaResponse> marcarEntrada(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(asistenciaService.marcarEntrada(usuario));
    }

    @PostMapping("/salida")
    public ResponseEntity<AsistenciaResponse> marcarSalida(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(asistenciaService.marcarSalida(usuario));
    }

    @PostMapping("/almuerzo/salida")
    public ResponseEntity<AsistenciaResponse> marcarSalidaAlmuerzo(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(asistenciaService.marcarSalidaAlmuerzo(usuario));
    }

    @PostMapping("/almuerzo/retorno")
    public ResponseEntity<AsistenciaResponse> marcarRetornoAlmuerzo(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(asistenciaService.marcarRetornoAlmuerzo(usuario));
    }

    @GetMapping("/mis")
    public ResponseEntity<List<AsistenciaResponse>> misAsistencias(
            Authentication authentication,
            @RequestParam(required = false) EstadoAsistencia estado) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(asistenciaService.misAsistencias(usuario, estado));
    }

    @GetMapping("/reporte")
    @PreAuthorize("hasAnyRole('ADMINISTRACION', 'JEFE')")
    public ResponseEntity<List<ReporteResponse>> reporte(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(asistenciaService.reporte(usuarioId, desde, hasta));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRACION', 'JEFE')")
    public ResponseEntity<List<AsistenciaResponse>> listar(
            @RequestParam(required = false) EstadoAsistencia estado) {
        return ResponseEntity.ok(asistenciaService.listar(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRACION', 'JEFE')")
    public ResponseEntity<AsistenciaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(asistenciaService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<AsistenciaResponse> crearManual(@Valid @RequestBody AsistenciaManualRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asistenciaService.crearManual(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<AsistenciaResponse> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody AsistenciaUpdateRequest request) {
        return ResponseEntity.ok(asistenciaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asistenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}