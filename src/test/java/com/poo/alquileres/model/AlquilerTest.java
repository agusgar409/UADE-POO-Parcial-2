package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoAlquiler;
import com.poo.alquileres.model.enums.TipoEquipo;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class AlquilerTest {

    private Equipo equipo;

    @Before
    public void setUp() {
        equipo = new Equipo("EQ-1", "Parlante", "500W",
                TipoEquipo.SONIDO, 1000.0, 10, false);
    }

    private Alquiler conUnDetalle(Alquiler alquiler) {
        alquiler.agregarDetalle(new DetalleAlquiler(equipo, 2)); // 2 * 1000
        return alquiler;
    }

    @Test
    public void polimorfismoDeRecargoPorTipo() {
        assertEquals(0.0, new AlquilerComun(1, LocalDate.now(), 1)
                .obtenerPorcentajeRecargo(), 0.001);
        assertEquals(AlquilerMasivo.RECARGO_MASIVO, new AlquilerMasivo(2, LocalDate.now(), 1)
                .obtenerPorcentajeRecargo(), 0.001);
        assertEquals(AlquilerCorporativo.RECARGO_CORPORATIVO,
                new AlquilerCorporativo(3, LocalDate.now(), 1).obtenerPorcentajeRecargo(), 0.001);
    }

    @Test
    public void calcularSubtotalSumaDetallesPorDias() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(1, LocalDate.now(), 3));
        // 2 * 1000 * 3 = 6000
        assertEquals(6000.0, alquiler.calcularSubtotal(), 0.001);
    }

    @Test
    public void importeTotalComunSinRecargoNiDescuento() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(1, LocalDate.now(), 2)); // subtotal 4000
        assertEquals(4000.0, alquiler.calcularImporteTotal(0), 0.001);
    }

    @Test
    public void importeTotalMasivoAplicaRecargo() {
        Alquiler alquiler = conUnDetalle(new AlquilerMasivo(1, LocalDate.now(), 1)); // subtotal 2000
        // 2000 + 10% = 2200
        assertEquals(2200.0, alquiler.calcularImporteTotal(0), 0.001);
    }

    @Test
    public void importeTotalCorporativoConDescuentoCliente() {
        Alquiler alquiler = conUnDetalle(new AlquilerCorporativo(1, LocalDate.now(), 1)); // subtotal 2000
        // 2000 + 15% recargo - 5% descuento = 2000 + 300 - 100 = 2200
        assertEquals(2200.0, alquiler.calcularImporteTotal(5), 0.001);
    }

    @Test
    public void importePendienteDescuentaSenia() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(1, LocalDate.now(), 1)); // subtotal 2000
        alquiler.registrarSenia(500);
        assertEquals(1500.0, alquiler.calcularImportePendiente(0), 0.001);
    }

    @Test
    public void cicloDeEstadosCompleto() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(1, LocalDate.now(), 1));
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
        Alquiler alquiler = conUnDetalle(new AlquilerComun(1, LocalDate.now(), 1));
        alquiler.confirmar();
        alquiler.entregar(); // requiere EN_PREPARACION
    }

    @Test(expected = IllegalStateException.class)
    public void noSePuedeCancelarUnAlquilerEntregado() {
        Alquiler alquiler = conUnDetalle(new AlquilerComun(1, LocalDate.now(), 1));
        alquiler.confirmar();
        alquiler.pasarAEnPreparacion();
        alquiler.entregar();
        alquiler.cancelar();
    }

    @Test
    public void calcularHorasAnticipacion() {
        LocalDate evento = LocalDate.of(2026, 1, 11);
        Alquiler alquiler = new AlquilerComun(1, evento, 1);
        // del 10 al 11 = 24 horas
        assertEquals(24, alquiler.calcularHorasAnticipacion(LocalDate.of(2026, 1, 10)));
    }
}
