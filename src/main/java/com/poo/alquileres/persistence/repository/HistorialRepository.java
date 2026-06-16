package com.poo.alquileres.persistence.repository;

import com.google.gson.reflect.TypeToken;
import com.poo.alquileres.model.HistorialCambioEstado;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.persistence.GsonFactory;
import com.poo.alquileres.persistence.JsonStore;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Persistencia de la auditoría de cambios de estado en historiales.json (append-only).
 */
public class HistorialRepository {

    private static final Type LIST_TYPE = new TypeToken<List<HistorialCambioEstado>>() {}.getType();

    private final JsonStore<HistorialCambioEstado> store =
            new JsonStore<>("historiales.json", LIST_TYPE, GsonFactory.base());

    public List<HistorialCambioEstado> findAll() {
        return store.readAll();
    }

    public HistorialCambioEstado save(HistorialCambioEstado historial) {
        List<HistorialCambioEstado> historiales = findAll();
        historiales.add(historial);
        store.writeAll(historiales);
        return historial;
    }

    public List<HistorialCambioEstado> findByEntidad(TipoEntidad tipoEntidad, String referencia) {
        return findAll().stream()
                .filter(h -> h.getTipoEntidad() == tipoEntidad)
                .filter(h -> h.getReferencia() != null && h.getReferencia().equals(referencia))
                .toList();
    }
}
