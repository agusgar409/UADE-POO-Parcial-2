package com.poo.alquileres.model;

import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Alquiler de gran volumen: aplica un recargo por logística/escala.
 */
@NoArgsConstructor
public class AlquilerMasivo extends Alquiler {

    /** Recargo por defecto para alquileres masivos (%). */
    public static final double RECARGO_MASIVO = 10.0;

    public AlquilerMasivo(Cliente cliente, LocalDate fechaEvento, int cantidadDias, double recargo) {
        super(cliente, fechaEvento, cantidadDias, recargo);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return getRecargo();
    }
}
