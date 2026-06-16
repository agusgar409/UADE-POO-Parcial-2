package com.poo.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoCliente {

    private double porcentaje;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    public boolean estaVigente(LocalDate fecha) {
        if (fecha == null || fechaDesde == null || fechaHasta == null) {
            return false;
        }
        return !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
    }
}
