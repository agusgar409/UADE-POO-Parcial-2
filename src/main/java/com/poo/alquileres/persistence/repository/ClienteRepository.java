package com.poo.alquileres.persistence.repository;

import com.google.gson.reflect.TypeToken;
import com.poo.alquileres.model.Cliente;
import com.poo.alquileres.persistence.GsonFactory;
import com.poo.alquileres.persistence.JsonStore;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia de clientes en clientes.json. Clave natural: dniCuit.
 */
public class ClienteRepository {

    private static final Type LIST_TYPE = new TypeToken<List<Cliente>>() {}.getType();

    private final JsonStore<Cliente> store =
            new JsonStore<>("clientes.json", LIST_TYPE, GsonFactory.base());

    public List<Cliente> findAll() {
        return store.readAll();
    }

    public Optional<Cliente> findByDniCuit(String dniCuit) {
        return findAll().stream()
                .filter(c -> c.getDniCuit() != null && c.getDniCuit().equals(dniCuit))
                .findFirst();
    }

    /** Inserta o actualiza el cliente (upsert por dniCuit). */
    public Cliente save(Cliente cliente) {
        List<Cliente> clientes = findAll();
        clientes.removeIf(c -> c.getDniCuit() != null && c.getDniCuit().equals(cliente.getDniCuit()));
        clientes.add(cliente);
        store.writeAll(clientes);
        return cliente;
    }

    public void deleteByDniCuit(String dniCuit) {
        List<Cliente> clientes = findAll();
        clientes.removeIf(c -> c.getDniCuit() != null && c.getDniCuit().equals(dniCuit));
        store.writeAll(clientes);
    }
}
