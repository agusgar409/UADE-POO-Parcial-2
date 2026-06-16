package com.poo.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAlquiler {

    private int cantidad;
    private double valorDiarioAplicado;
    private Equipo equipo;

    public DetalleAlquiler(Equipo equipo, int cantidad) {
        this(equipo, cantidad, equipo != null ? equipo.getValorDiario() : 0.0);
    }

    /** Constructor con el valor diario explícito (firma del diagrama de secuencia). */
    public DetalleAlquiler(Equipo equipo, int cantidad, double valorDiarioAplicado) {
        this.equipo = equipo;
        this.cantidad = cantidad;
        this.valorDiarioAplicado = valorDiarioAplicado;
    }

    public double calcularSubtotal(int cantidadDias) {
        return cantidad * valorDiarioAplicado * cantidadDias;
    }

    public int obtenerCantidad() {
        return cantidad;
    }

    public Equipo obtenerEquipo() {
        return equipo;
    }
}
