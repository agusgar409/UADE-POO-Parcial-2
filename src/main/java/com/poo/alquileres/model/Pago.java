package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoPago;
import com.poo.alquileres.model.enums.MedioPago;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Pago asociado a un alquiler (seña o saldo). Tiene su propio ciclo de estado.
 */
@Data
@NoArgsConstructor
public class Pago {

    private int id;
    private LocalDate fecha;
    private double importe;
    private MedioPago medioPago;
    private EstadoPago estado = EstadoPago.REGISTRADO;
    private String usuarioRegistro;

    public Pago(int id, double importe, MedioPago medioPago, String usuarioRegistro) {
        this.id = id;
        this.fecha = LocalDate.now();
        this.importe = importe;
        this.medioPago = medioPago;
        this.estado = EstadoPago.REGISTRADO;
        this.usuarioRegistro = usuarioRegistro;
    }

    public void confirmar() {
        if (estado == EstadoPago.ANULADO) {
            throw new IllegalStateException("No se puede confirmar un pago anulado.");
        }
        this.estado = EstadoPago.CONFIRMADO;
    }

    public void anular() {
        this.estado = EstadoPago.ANULADO;
    }
}
