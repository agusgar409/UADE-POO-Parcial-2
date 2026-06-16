package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.TipoEquipo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DetalleAlquilerTest {

    @Test
    public void calcularSubtotalUsaCantidadValorYDias() {
        Equipo equipo = new Equipo("EQ-1", "Proyector", "Full HD",
                TipoEquipo.PROYECTOR, 800.0, 10, false);
        DetalleAlquiler detalle = new DetalleAlquiler(equipo, 2);

        // 2 unidades * 800 * 3 días = 4800
        assertEquals(4800.0, detalle.calcularSubtotal(3), 0.001);
    }

    @Test
    public void valorDiarioSeCongelaAlCrearElDetalle() {
        Equipo equipo = new Equipo("EQ-2", "Pantalla", "LED",
                TipoEquipo.PANTALLA, 500.0, 4, false);
        DetalleAlquiler detalle = new DetalleAlquiler(equipo, 1);

        equipo.setValorDiario(9999.0); // cambia el equipo después
        assertEquals(500.0, detalle.getValorDiarioAplicado(), 0.001);
    }
}
