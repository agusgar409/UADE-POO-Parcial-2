package com.poo.alquileres.controller;

import com.poo.alquileres.model.HistorialCambioEstado;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.persistence.repository.HistorialRepository;

import java.time.LocalDateTime;
import java.util.List;

public class HistorialController {

    private static HistorialController instance;

    private final HistorialRepository repository = new HistorialRepository();

    private HistorialController() {
    }

    public static synchronized HistorialController getInstance() {
        if (instance == null) {
            instance = new HistorialController();
        }
        return instance;
    }

    /**
     * Persiste un historial ya construido y asociado (mensaje "agregarHistorial" del diagrama).
     */
    public HistorialCambioEstado agregarHistorial(HistorialCambioEstado historial) {
        return repository.save(historial);
    }

    public HistorialCambioEstado registrar(TipoEntidad tipoEntidad, String referencia,
                                           String estadoAnterior, String estadoNuevo,
                                           String usuario) {
        HistorialCambioEstado historial = new HistorialCambioEstado(
                LocalDateTime.now(), estadoAnterior, estadoNuevo,
                tipoEntidad, referencia, usuario);
        return repository.save(historial);
    }

    public List<HistorialCambioEstado> listarTodos() {
        return repository.findAll();
    }

    public List<HistorialCambioEstado> listarPorEntidad(TipoEntidad tipoEntidad, String referencia) {
        return repository.findByEntidad(tipoEntidad, referencia);
    }
}
