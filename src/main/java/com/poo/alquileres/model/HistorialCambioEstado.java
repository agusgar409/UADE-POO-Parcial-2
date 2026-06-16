package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.TipoEntidad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de auditoría de un cambio de estado. Cada historial corresponde a una sola
 * entidad afectada; se conserva tipoEntidad y referencia como datos de auditoría.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambioEstado {

    private LocalDateTime fechaCambio;
    private String estadoAnterior;
    private String estadoNuevo;
    private TipoEntidad tipoEntidad;
    private String referencia;
    private String usuarioResponsable;
}
