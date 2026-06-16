package com.poo.alquileres.persistence.repository;

import com.google.gson.reflect.TypeToken;
import com.poo.alquileres.model.Alquiler;
import com.poo.alquileres.persistence.GsonFactory;
import com.poo.alquileres.persistence.JsonStore;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class AlquilerRepository {

    private static final Type LIST_TYPE = new TypeToken<List<Alquiler>>() {}.getType();

    private final JsonStore<Alquiler> store =
            new JsonStore<>("alquileres.json", LIST_TYPE, GsonFactory.conAlquileres());

    public List<Alquiler> findAll() {
        return store.readAll();
    }

    public Optional<Alquiler> findById(int id) {
        return findAll().stream()
                .filter(a -> a.getId() == id)
                .findFirst();
    }

    public int nextId() {
        return findAll().stream()
                .mapToInt(Alquiler::getId)
                .max()
                .orElse(0) + 1;
    }

    public Alquiler save(Alquiler alquiler) {
        List<Alquiler> alquileres = findAll();
        alquileres.removeIf(a -> a.getId() == alquiler.getId());
        alquileres.add(alquiler);
        store.writeAll(alquileres);
        return alquiler;
    }

    public void deleteById(int id) {
        List<Alquiler> alquileres = findAll();
        alquileres.removeIf(a -> a.getId() == id);
        store.writeAll(alquileres);
    }
}
