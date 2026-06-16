package com.poo.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Descuento porcentual aplicable a un cliente dentro de un rango de fechas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoCliente {

    private double porcentaje;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    /**
     * Indica si el descuento está vigente en la fecha indicada (rango inclusivo).
     */
    public boolean estaVigente(LocalDate fecha) {
        if (fecha == null || fechaDesde == null || fechaHasta == null) {
            return false;
        }
        return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
    }
}
