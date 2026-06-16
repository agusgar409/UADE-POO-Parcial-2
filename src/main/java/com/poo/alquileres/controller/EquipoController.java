package com.poo.alquileres.controller;

import com.poo.alquileres.model.Equipo;
import com.poo.alquileres.model.HistorialCambioEstado;
import com.poo.alquileres.model.enums.EstadoEquipo;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.model.enums.TipoEquipo;
import com.poo.alquileres.persistence.repository.EquipoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EquipoController {

    private static EquipoController instance;

    private final EquipoRepository repository = new EquipoRepository();
    private final HistorialController historialController = HistorialController.getInstance();

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
        for (Equipo equipoActual : repository.findAll()) {
            if (equipoActual.coincideCodigo(codigo)) {
                throw new IllegalStateException("Ya existe un equipo con código " + codigo);
            }
        }
        Equipo equipo = new Equipo(codigo, nombre, descripcion, tipoEquipo,
                valorDiario, stockInicial, requiereInstalacion);
        repository.save(equipo);

        HistorialCambioEstado historial = new HistorialCambioEstado(
                LocalDateTime.now(), "-", equipo.getEstado().name(),
                TipoEntidad.EQUIPO, codigo, usuario);
        historial.asociarEquipo(equipo);
        historialController.agregarHistorial(historial);
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
        for (Equipo equipoActual : repository.findAll()) {
            boolean coincideTipo = tipoEvento == null || equipoActual.coincideTipoEvento(tipoEvento);
            if (coincideTipo && equipoActual.estaDisponible(1)) {
                disponibles.add(equipoActual);
            }
        }
        return disponibles;
    }

    public void cambiarEstado(String codigo, EstadoEquipo estadoNuevo, String usuario) {
        Equipo equipo = buscarPorCodigo(codigo);
        String anterior = equipo.getEstado().name();
        equipo.cambiarEstado(estadoNuevo);
        repository.save(equipo);
        historialController.registrar(TipoEntidad.EQUIPO, codigo, anterior,
                equipo.getEstado().name(), usuario);
    }
}
