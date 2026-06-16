package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoCliente;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Cliente {

    private String dniCuit;
    private String nombreRazonSocial;
    private String telefono;
    private String email;
    private String direccion;
    private EstadoCliente estado = EstadoCliente.ACTIVO;
    private double creditoAFavor;
    private List<DescuentoCliente> descuentos = new ArrayList<>();

    public Cliente(String dniCuit, String nombreRazonSocial, String telefono,
                   String email, String direccion) {
        this.dniCuit = dniCuit;
        this.nombreRazonSocial = nombreRazonSocial;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.estado = EstadoCliente.ACTIVO;
        this.creditoAFavor = 0.0;
        this.descuentos = new ArrayList<>();
    }

    public void activar() {
        this.estado = EstadoCliente.ACTIVO;
    }

    public void inactivar() {
        this.estado = EstadoCliente.INACTIVO;
    }

    public void agregarCredito(double importe) {
        if (importe < 0) {
            throw new IllegalArgumentException("El crédito a agregar no puede ser negativo.");
        }
        this.creditoAFavor += importe;
    }

    public void agregarDescuento(DescuentoCliente descuento) {
        if (descuentos == null) {
            descuentos = new ArrayList<>();
        }
        descuentos.add(descuento);
    }

    public double obtenerDescuentoVigente(LocalDate fecha) {
        if (descuentos == null) {
            return 0.0;
        }
        return descuentos.stream()
                .filter(d -> d.estaVigente(fecha))
                .mapToDouble(DescuentoCliente::getPorcentaje)
                .max()
                .orElse(0.0);
    }

    public boolean estaActivo() {
        return estado == EstadoCliente.ACTIVO;
    }

    /** Indica si el DNI/CUIT del cliente coincide con el buscado (nombre del diagrama). */
    public boolean coincideDniCuit(String dniCuit) {
        return this.dniCuit != null && this.dniCuit.equals(dniCuit);
    }
}
