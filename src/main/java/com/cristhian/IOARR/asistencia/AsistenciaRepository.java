package com.cristhian.IOARR.asistencia;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cristhian.IOARR.user.Usuario;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long>, JpaSpecificationExecutor<Asistencia> {

    Optional<Asistencia> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);

    boolean existsByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
}