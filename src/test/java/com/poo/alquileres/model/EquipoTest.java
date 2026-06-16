package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.EstadoEquipo;
import com.poo.alquileres.model.enums.TipoEquipo;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EquipoTest {

    private Equipo equipo;

    @Before
    public void setUp() {
        equipo = new Equipo("EQ-1", "Parlante", "Parlante 500W",
                TipoEquipo.SONIDO, 1000.0, 5, false);
    }

    @Test
    public void estaDisponibleConStockSuficiente() {
        assertTrue(equipo.estaDisponible(5));
        assertFalse(equipo.estaDisponible(6));
    }

    @Test
    public void noEstaDisponibleSiEstadoNoEsDisponible() {
        equipo.cambiarEstado(EstadoEquipo.MANTENIMIENTO);
        assertFalse(equipo.estaDisponible(1));
    }

    @Test
    public void reservarStockDescuenta() {
        equipo.reservarStock(2);
        assertEquals(3, equipo.getStockDisponible());
    }

    @Test(expected = IllegalStateException.class)
    public void reservarStockInsuficienteLanzaExcepcion() {
        equipo.reservarStock(10);
    }

    @Test
    public void liberarStockDevuelveUnidades() {
        equipo.reservarStock(3);
        equipo.liberarStock(2);
        assertEquals(4, equipo.getStockDisponible());
    }
}
