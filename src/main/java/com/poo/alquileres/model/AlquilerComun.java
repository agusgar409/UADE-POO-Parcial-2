package com.poo.alquileres.model;

import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Alquiler estándar: sin recargo por tipo.
 */
@NoArgsConstructor
public class AlquilerComun extends Alquiler {

    public AlquilerComun(int id, LocalDate fechaEvento, int cantidadDias) {
        super(id, fechaEvento, cantidadDias);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return 0.0;
    }
}
