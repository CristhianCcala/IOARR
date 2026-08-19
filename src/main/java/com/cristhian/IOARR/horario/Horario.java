package com.cristhian.IOARR.horario;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "horarios")
@Getter
@Setter
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida_almuerzo", nullable = false)
    private LocalTime horaSalidaAlmuerzo;

    @Column(name = "hora_entrada_almuerzo", nullable = false)
    private LocalTime horaEntradaAlmuerzo;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(nullable = false)
    private int toleranciaMinutos;

    @Column
    private double latitud;

    @Column
    private double longitud;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }
}