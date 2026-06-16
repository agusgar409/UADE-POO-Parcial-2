package com.poo.alquileres.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClienteTest {

    private Cliente cliente;

    @Before
    public void setUp() {
        cliente = new Cliente("20304050", "Juan Pérez", "11111111",
                "juan@mail.com", "Calle 123");
    }

    @Test
    public void clienteNuevoEstaActivo() {
        assertTrue(cliente.estaActivo());
    }

    @Test
    public void inactivarYActivarCambianEstado() {
        cliente.inactivar();
        assertFalse(cliente.estaActivo());
        cliente.activar();
        assertTrue(cliente.estaActivo());
    }

    @Test
    public void agregarCreditoAcumula() {
        cliente.agregarCredito(500);
        cliente.agregarCredito(250);
        assertEquals(750.0, cliente.getCreditoAFavor(), 0.001);
    }

    @Test
    public void descuentoVigenteDevuelveElPorcentaje() {
        LocalDate hoy = LocalDate.now();
        cliente.agregarDescuento(new DescuentoCliente(
                10.0, hoy.minusDays(1), hoy.plusDays(1)));
        assertEquals(10.0, cliente.obtenerDescuentoVigente(hoy), 0.001);
    }

    @Test
    public void descuentoExpiradoNoAplica() {
        LocalDate hoy = LocalDate.now();
        cliente.agregarDescuento(new DescuentoCliente(
                10.0, hoy.minusDays(10), hoy.minusDays(5)));
        assertEquals(0.0, cliente.obtenerDescuentoVigente(hoy), 0.001);
    }

    @Test
    public void tomaElMayorDescuentoVigente() {
        LocalDate hoy = LocalDate.now();
        cliente.agregarDescuento(new DescuentoCliente(10.0, hoy.minusDays(1), hoy.plusDays(1)));
        cliente.agregarDescuento(new DescuentoCliente(25.0, hoy.minusDays(1), hoy.plusDays(1)));
        assertEquals(25.0, cliente.obtenerDescuentoVigente(hoy), 0.001);
    }
}
