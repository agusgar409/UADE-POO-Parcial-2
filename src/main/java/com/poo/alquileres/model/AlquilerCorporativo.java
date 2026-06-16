package com.poo.alquileres.model;

import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
public class AlquilerCorporativo extends Alquiler {

    public static final double RECARGO_CORPORATIVO = 15.0;

    public AlquilerCorporativo(Cliente cliente, LocalDate fechaEvento, int cantidadDias, double recargo) {
        super(cliente, fechaEvento, cantidadDias, recargo);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return getRecargo();
    }
}
