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

    public AlquilerCorporativo(Cliente cliente, LocalDate fechaEvento, int cantidadDias, double recargo) {
        super(cliente, fechaEvento, cantidadDias, recargo);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return getRecargo();
    }
}
