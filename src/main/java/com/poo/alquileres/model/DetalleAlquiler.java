package com.poo.alquileres.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Línea de un alquiler: un equipo, la cantidad reservada y el valor diario aplicado
 * (se "congela" el valor diario al momento de la solicitud).
 */
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

    /**
     * Subtotal de la línea = cantidad * valorDiarioAplicado * cantidadDias.
     */
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
