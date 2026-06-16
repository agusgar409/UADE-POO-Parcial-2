package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoAlquiler;
import com.poo.alquileres.model.enums.TipoEquipo;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class AlquilerTest {

    private Equipo equipo;
    private Cliente cliente;

    @Before
    public void setUp() {
        equipo = new Equipo("EQ-1", "Parlante", "500W",
                TipoEquipo.SONIDO, 1000.0, 10, false);
        cliente = new Cliente("20-1", "ACME", "111", "a@a.com", "Calle 1");
    }

    private Alquiler conUnDetalle(Alquiler alquiler) {
        alquiler.agregarDetalle(new DetalleAlquiler(equipo, 2)); // 2 * 1000
        return alquiler;
    }

    @Test
    public void polimorfismoDeRecargoPorTipo() {
        assertEquals(0.0,
                new AlquilerComun(cliente, LocalDate.now(), 1, 0.0).obtenerPorcentajeRecargo(),
                0.001);
        assertEquals(AlquilerMasivo.RECARGO_MASIVO,
                new AlquilerMasivo(cliente, LocalDate.now(), 1, AlquilerMasivo.RECARGO_MASIVO)
                        .obtenerPorcentajeRecargo(), 0.001);
        assertEquals(AlquilerCorporativo.RECARGO_CORPORATIVO,
                new AlquilerCorporativo(cliente, LocalDate.now(), 1, AlquilerCorporativo.RECARGO_CORPORATIVO)
                        .obtenerPorcentajeRecargo(), 0.001);
    }

    @Test
    public void calcularSubtotalSumaDetallesPorDias() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(cliente, LocalDate.now(), 3, 0.0));
        // 2 * 1000 * 3 = 6000
        assertEquals(6000.0, alquiler.calcularSubtotal(), 0.001);
    }

    @Test
    public void importeTotalComunSinRecargoNiDescuento() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(cliente, LocalDate.now(), 2, 0.0)); // subtotal 4000
        assertEquals(4000.0, alquiler.calcularImporteTotal(0), 0.001);
    }

    @Test
    public void importeTotalMasivoAplicaRecargo() {
        Alquiler alquiler = conUnDetalle(
                new AlquilerMasivo(cliente, LocalDate.now(), 1, 10.0)); // subtotal 2000
        // 2000 + 10% = 2200
        assertEquals(2200.0, alquiler.calcularImporteTotal(0), 0.001);
    }

    @Test
    public void importeTotalCorporativoConDescuentoCliente() {
        Alquiler alquiler = conUnDetalle(
                new AlquilerCorporativo(cliente, LocalDate.now(), 1, 15.0)); // subtotal 2000
        // 2000 + 15% recargo - 5% descuento = 2000 + 300 - 100 = 2200
        assertEquals(2200.0, alquiler.calcularImporteTotal(5), 0.001);
    }

    @Test
    public void importePendienteDescuentaSenia() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(cliente, LocalDate.now(), 1, 0.0)); // subtotal 2000
        alquiler.registrarSenia(500);
        assertEquals(1500.0, alquiler.calcularImportePendiente(0), 0.001);
    }

    @Test
    public void cicloDeEstadosCompleto() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(cliente, LocalDate.now(), 1, 0.0));
        assertEquals(EstadoAlquiler.INGRESADO, alquiler.getEstado());
        alquiler.confirmar();
        assertEquals(EstadoAlquiler.CONFIRMADO, alquiler.getEstado());
        alquiler.pasarAEnPreparacion();
        assertEquals(EstadoAlquiler.EN_PREPARACION, alquiler.getEstado());
        alquiler.entregar();
        assertEquals(EstadoAlquiler.ENTREGADO, alquiler.getEstado());
        alquiler.finalizar();
        assertEquals(EstadoAlquiler.FINALIZADO, alquiler.getEstado());
    }

    @Test(expected = IllegalStateException.class)
    public void noSePuedeEntregarSinPreparar() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(cliente, LocalDate.now(), 1, 0.0));
        alquiler.confirmar();
        alquiler.entregar(); // requiere EN_PREPARACION
    }

    @Test(expected = IllegalStateException.class)
    public void noSePuedeCancelarUnAlquilerEntregado() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(cliente, LocalDate.now(), 1, 0.0));
        alquiler.confirmar();
        alquiler.pasarAEnPreparacion();
        alquiler.entregar();
        alquiler.cancelar();
    }

    @Test
    public void calcularHorasAnticipacion() {
        LocalDate evento = LocalDate.of(2026, 1, 11);
        Alquiler alquiler = new AlquilerComun(cliente, evento, 1, 0.0);
        // del 10 al 11 = 24 horas
        assertEquals(24, alquiler.calcularHorasAnticipacion(LocalDate.of(2026, 1, 10)));
    }

    @Test
    public void coincideIdYObtenerCliente() {
        Alquiler alquiler = new AlquilerComun(cliente, LocalDate.now(), 1, 0.0);
        alquiler.setId(7);
        assertEquals(7, alquiler.getId());
        org.junit.Assert.assertTrue(alquiler.coincideId(7));
        org.junit.Assert.assertEquals(cliente, alquiler.obtenerCliente());
    }
}
