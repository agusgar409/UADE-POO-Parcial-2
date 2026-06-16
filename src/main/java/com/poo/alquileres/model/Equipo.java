package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoEquipo;
import com.poo.alquileres.model.enums.TipoEquipo;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Equipo disponible para alquiler. Se identifica por su código.
 */
@Data
@NoArgsConstructor
public class Equipo {

    private String codigo;
    private String nombre;
    private String descripcion;
    private TipoEquipo tipoEquipo;
    private EstadoEquipo estado = EstadoEquipo.DISPONIBLE;
    private double valorDiario;
    private int stockDisponible;
    private boolean requiereInstalacion;

    public Equipo(String codigo, String nombre, String descripcion, TipoEquipo tipoEquipo,
                  double valorDiario, int stockDisponible, boolean requiereInstalacion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoEquipo = tipoEquipo;
        this.estado = EstadoEquipo.DISPONIBLE;
        this.valorDiario = valorDiario;
        this.stockDisponible = stockDisponible;
        this.requiereInstalacion = requiereInstalacion;
    }

    /**
     * Hay disponibilidad si el equipo está DISPONIBLE y el stock alcanza la cantidad pedida.
     */
    public boolean estaDisponible(int cantidad) {
        return estado == EstadoEquipo.DISPONIBLE && stockDisponible >= cantidad;
    }

    public void reservarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reservar debe ser positiva.");
        }
        if (!estaDisponible(cantidad)) {
            throw new IllegalStateException(
                    "Stock insuficiente o equipo no disponible: " + nombre);
        }
        this.stockDisponible -= cantidad;
    }

    public void liberarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a liberar debe ser positiva.");
        }
        this.stockDisponible += cantidad;
    }

    public void cambiarEstado(EstadoEquipo estadoNuevo) {
        this.estado = estadoNuevo;
    }
}
