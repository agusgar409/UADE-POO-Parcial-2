package com.poo.alquileres.controller;

import com.poo.alquileres.model.Equipo;
import com.poo.alquileres.model.enums.EstadoEquipo;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.model.enums.TipoEquipo;
import com.poo.alquileres.persistence.repository.EquipoRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller Singleton de equipos. Las vistas lo usan vía getInstance().
 */
public class EquipoController {

    private static EquipoController instance;

    private final EquipoRepository repository = new EquipoRepository();
    private final HistorialController historial = HistorialController.getInstance();

    private EquipoController() {
    }

    public static synchronized EquipoController getInstance() {
        if (instance == null) {
            instance = new EquipoController();
        }
        return instance;
    }

    public Equipo registrarEquipo(String codigo, String nombre, String descripcion,
                                  TipoEquipo tipoEquipo, double valorDiario, int stockInicial,
                                  boolean requiereInstalacion, String usuario) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del equipo es obligatorio.");
        }
        if (repository.findByCodigo(codigo).isPresent()) {
            throw new IllegalStateException("Ya existe un equipo con código " + codigo);
        }
        Equipo equipo = new Equipo(codigo, nombre, descripcion, tipoEquipo,
                valorDiario, stockInicial, requiereInstalacion);
        repository.save(equipo);
        historial.registrar(TipoEntidad.EQUIPO, codigo, "-",
                equipo.getEstado().name(), usuario);
        return equipo;
    }

    public List<Equipo> listarEquipos() {
        return repository.findAll();
    }

    public Equipo buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe equipo con código " + codigo));
    }

    /**
     * Equipos del tipo pedido que tengan al menos una unidad disponible para el evento.
     * Sigue el UC5: por cada equipo se valida que coincida el tipo y que esté disponible
     * (estaDisponible(1)). fechaEvento/cantidadDias se reciben según la firma del enunciado.
     */
    public List<Equipo> consultarEquiposDisponibles(LocalDate fechaEvento, int cantidadDias,
                                                     TipoEquipo tipoEvento) {
        List<Equipo> disponibles = new ArrayList<>();
        for (Equipo equipo : repository.findAll()) {
            boolean coincideTipo = tipoEvento == null || equipo.getTipoEquipo() == tipoEvento;
            if (coincideTipo && equipo.estaDisponible(1)) {
                disponibles.add(equipo);
            }
        }
        return disponibles;
    }

    public void cambiarEstado(String codigo, EstadoEquipo estadoNuevo, String usuario) {
        Equipo equipo = buscarPorCodigo(codigo);
        String anterior = equipo.getEstado().name();
        equipo.cambiarEstado(estadoNuevo);
        repository.save(equipo);
        historial.registrar(TipoEntidad.EQUIPO, codigo, anterior,
                equipo.getEstado().name(), usuario);
    }
}
