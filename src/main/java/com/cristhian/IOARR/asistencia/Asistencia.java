package com.cristhian.IOARR.asistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.cristhian.IOARR.user.Usuario;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "asistencias",
        uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "fecha" }))
@Getter
@Setter
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "hora_salida_almuerzo")
    private LocalTime horaSalidaAlmuerzo;

    @Column(name = "hora_entrada_almuerzo")
    private LocalTime horaEntradaAlmuerzo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsistencia estado;

    @Column(length = 255)
    private String observacion;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }
}