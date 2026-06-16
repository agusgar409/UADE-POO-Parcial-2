package com.poo.alquileres.model;

import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Alquiler para empresas: aplica recargo corporativo (facturación, garantías, etc.).
 */
@NoArgsConstructor
public class AlquilerCorporativo extends Alquiler {

    /** Recargo por defecto para alquileres corporativos (%). */
    public static final double RECARGO_CORPORATIVO = 15.0;

    public AlquilerCorporativo(int id, LocalDate fechaEvento, int cantidadDias) {
        super(id, fechaEvento, cantidadDias);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return RECARGO_CORPORATIVO;
    }
}
