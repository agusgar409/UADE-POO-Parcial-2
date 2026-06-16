package com.poo.alquileres.persistence.repository;

import com.google.gson.reflect.TypeToken;
import com.poo.alquileres.model.Equipo;
import com.poo.alquileres.persistence.GsonFactory;
import com.poo.alquileres.persistence.JsonStore;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class EquipoRepository {

    private static final Type LIST_TYPE = new TypeToken<List<Equipo>>() {}.getType();

    private final JsonStore<Equipo> store =
            new JsonStore<>("equipos.json", LIST_TYPE, GsonFactory.base());

    public List<Equipo> findAll() {
        return store.readAll();
    }

    public Optional<Equipo> findByCodigo(String codigo) {
        return findAll().stream()
                .filter(e -> e.getCodigo() != null && e.getCodigo().equals(codigo))
                .findFirst();
    }

    public Equipo save(Equipo equipo) {
        List<Equipo> equipos = findAll();
        equipos.removeIf(e -> e.getCodigo() != null && e.getCodigo().equals(equipo.getCodigo()));
        equipos.add(equipo);
        store.writeAll(equipos);
        return equipo;
    }

    public void saveAll(List<Equipo> equipos) {
        store.writeAll(equipos);
    }

    public void deleteByCodigo(String codigo) {
        List<Equipo> equipos = findAll();
        equipos.removeIf(e -> e.getCodigo() != null && e.getCodigo().equals(codigo));
        store.writeAll(equipos);
    }
}
