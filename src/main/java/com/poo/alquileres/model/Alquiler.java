package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoAlquiler;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Alquiler de equipos para un evento. Clase abstracta: el porcentaje de recargo depende
 * del tipo concreto (Común, Masivo, Corporativo) -> polimorfismo.
 */
@Data
@NoArgsConstructor
public abstract class Alquiler {

    private int id;
    private String clienteDniCuit;
    private LocalDate fechaSolicitud;
    private LocalDate fechaEvento;
    private int cantidadDias;
    private EstadoAlquiler estado = EstadoAlquiler.INGRESADO;
    private double seniaAbonada;
    private double porcentajeRecargoAplicado;
    private double importeTotal;
    private double importePendiente;
    private List<DetalleAlquiler> detalles = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();

    protected Alquiler(int id, LocalDate fechaEvento, int cantidadDias) {
        this.id = id;
        this.fechaSolicitud = LocalDate.now();
        this.fechaEvento = fechaEvento;
        this.cantidadDias = cantidadDias;
        this.estado = EstadoAlquiler.INGRESADO;
        this.detalles = new ArrayList<>();
        this.pagos = new ArrayList<>();
    }

    // ----- Composición -----

    public void agregarDetalle(DetalleAlquiler detalle) {
        if (detalles == null) {
            detalles = new ArrayList<>();
        }
        detalles.add(detalle);
    }

    public void registrarPago(Pago pago) {
        if (pagos == null) {
            pagos = new ArrayList<>();
        }
        pagos.add(pago);
    }

    public void registrarSenia(double importe) {
        if (importe < 0) {
            throw new IllegalArgumentException("La seña no puede ser negativa.");
        }
        this.seniaAbonada += importe;
    }

    // ----- Transiciones de estado -----

    public void confirmar() {
        exigirEstado(EstadoAlquiler.INGRESADO, "confirmar");
        this.estado = EstadoAlquiler.CONFIRMADO;
    }

    public void pasarAEnPreparacion() {
        exigirEstado(EstadoAlquiler.CONFIRMADO, "pasar a en preparación");
        this.estado = EstadoAlquiler.EN_PREPARACION;
    }

    public void entregar() {
        exigirEstado(EstadoAlquiler.EN_PREPARACION, "entregar");
        this.estado = EstadoAlquiler.ENTREGADO;
    }

    public void finalizar() {
        exigirEstado(EstadoAlquiler.ENTREGADO, "finalizar");
        this.estado = EstadoAlquiler.FINALIZADO;
    }

    public void cancelar() {
        if (estado == EstadoAlquiler.ENTREGADO || estado == EstadoAlquiler.FINALIZADO
                || estado == EstadoAlquiler.CANCELADO) {
            throw new IllegalStateException(
                    "No se puede cancelar un alquiler en estado " + estado);
        }
        this.estado = EstadoAlquiler.CANCELADO;
    }

    private void exigirEstado(EstadoAlquiler esperado, String accion) {
        if (estado != esperado) {
            throw new IllegalStateException(
                    "No se puede " + accion + " un alquiler en estado " + estado
                            + " (se requiere " + esperado + ").");
        }
    }

    // ----- Cálculos -----

    /**
     * Suma de los subtotales de cada detalle por la cantidad de días.
     */
    public double calcularSubtotal() {
        if (detalles == null) {
            return 0.0;
        }
        return detalles.stream()
                .mapToDouble(d -> d.calcularSubtotal(cantidadDias))
                .sum();
    }

    /**
     * Importe total = subtotal + recargo - descuento del cliente.
     * @param descuentoCliente porcentaje de descuento (0..100).
     */
    public double calcularImporteTotal(double descuentoCliente) {
        double subtotal = calcularSubtotal();
        double porcentajeRecargo = obtenerPorcentajeRecargo();
        double recargo = subtotal * porcentajeRecargo / 100.0;
        double descuento = subtotal * descuentoCliente / 100.0;

        this.porcentajeRecargoAplicado = porcentajeRecargo;
        this.importeTotal = subtotal + recargo - descuento;
        return this.importeTotal;
    }

    /**
     * Importe pendiente = total - seña abonada.
     */
    public double calcularImportePendiente(double descuentoCliente) {
        this.importePendiente = calcularImporteTotal(descuentoCliente) - seniaAbonada;
        return this.importePendiente;
    }

    /**
     * Horas de anticipación entre la cancelación y el evento (útil para penalidades).
     */
    public int calcularHorasAnticipacion(LocalDate fechaCancelacion) {
        if (fechaCancelacion == null || fechaEvento == null) {
            return 0;
        }
        long horas = ChronoUnit.HOURS.between(
                fechaCancelacion.atStartOfDay(),
                fechaEvento.atStartOfDay());
        return (int) horas;
    }

    /**
     * Porcentaje de recargo según el tipo concreto de alquiler. Polimórfico.
     */
    public abstract double obtenerPorcentajeRecargo();
}
