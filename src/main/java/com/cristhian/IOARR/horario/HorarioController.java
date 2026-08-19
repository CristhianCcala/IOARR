package com.cristhian.IOARR.horario;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cristhian.IOARR.dto.HorarioRequest;
import com.cristhian.IOARR.dto.HorarioResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public ResponseEntity<HorarioResponse> obtener() {
        return ResponseEntity.ok(horarioService.obtener());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMINISTRACION')")
    public ResponseEntity<HorarioResponse> actualizar(@Valid @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.actualizar(request));
    }
}