package com.poo.alquileres.controller;

import com.poo.alquileres.model.HistorialCambioEstado;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.persistence.repository.HistorialRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller Singleton de auditoría. Centraliza el registro de cambios de estado de
 * cualquier entidad del sistema.
 */
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
     * Registra un cambio de estado para auditoría.
     */
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
