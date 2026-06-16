package com.poo.alquileres.model;

import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
public class AlquilerMasivo extends Alquiler {

    public static final double RECARGO_MASIVO = 10.0;

    public AlquilerMasivo(Cliente cliente, LocalDate fechaEvento, int cantidadDias, double recargo) {
        super(cliente, fechaEvento, cantidadDias, recargo);
    }

    @Override
    public double obtenerPorcentajeRecargo() {
        return getRecargo();
    }
}
