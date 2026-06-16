package com.poo.alquileres.model;

import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Alquiler estándar: sin recargo por tipo (recargo = 0).
 */
@NoArgsConstructor
public class AlquilerComun extends Alquiler {

    public AlquilerComun(Cliente cliente, LocalDate fechaEvento, int cantidadDias, double recargo) {
        super(cliente, fechaEvento, cantidadDias, recargo);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return getRecargo();
    }
}
