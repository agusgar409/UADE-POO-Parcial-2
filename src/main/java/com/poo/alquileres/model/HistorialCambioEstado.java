package com.poo.alquileres.model;

import com.poo.alquileres.model.enums.TipoEntidad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambioEstado {

    private LocalDateTime fechaCambio;
    private String estadoAnterior;
    private String estadoNuevo;
    private TipoEntidad tipoEntidad;
    private String referencia;
    private String usuarioResponsable;

    // ----- Asociación con la entidad afectada (nombres del diagrama de secuencia) -----

    public void asociarCliente(Cliente cliente) {
        this.tipoEntidad = TipoEntidad.CLIENTE;
        this.referencia = cliente != null ? cliente.getDniCuit() : null;
    }

    public void asociarEquipo(Equipo equipo) {
        this.tipoEntidad = TipoEntidad.EQUIPO;
        this.referencia = equipo != null ? equipo.getCodigo() : null;
    }

    public void asociarAlquiler(Alquiler alquiler) {
        this.tipoEntidad = TipoEntidad.ALQUILER;
        this.referencia = alquiler != null ? String.valueOf(alquiler.getId()) : null;
    }

    public void asociarPago(Pago pago) {
        this.tipoEntidad = TipoEntidad.PAGO;
        this.referencia = pago != null ? String.valueOf(pago.getId()) : null;
    }
}
