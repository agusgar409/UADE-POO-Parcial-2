package com.poo.alquileres.controller;

import com.poo.alquileres.model.Cliente;
import com.poo.alquileres.model.DescuentoCliente;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.persistence.repository.ClienteRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller Singleton de clientes. Las vistas lo usan vía getInstance().
 */
public class ClienteController {

    private static ClienteController instance;

    private final ClienteRepository repository = new ClienteRepository();
    private final HistorialController historial = HistorialController.getInstance();

    private ClienteController() {
    }

    public static synchronized ClienteController getInstance() {
        if (instance == null) {
            instance = new ClienteController();
        }
        return instance;
    }

    public Cliente registrarCliente(String dniCuit, String nombreRazonSocial, String telefono,
                                    String email, String direccion, String usuario) {
        if (dniCuit == null || dniCuit.isBlank()) {
            throw new IllegalArgumentException("El DNI/CUIT es obligatorio.");
        }
        if (repository.findByDniCuit(dniCuit).isPresent()) {
            throw new IllegalStateException("Ya existe un cliente con DNI/CUIT " + dniCuit);
        }
        Cliente cliente = new Cliente(dniCuit, nombreRazonSocial, telefono, email, direccion);
        cliente.activar();
        repository.save(cliente);
        historial.registrar(TipoEntidad.CLIENTE, dniCuit, "-",
                cliente.getEstado().name(), usuario);
        return cliente;
    }

    public List<Cliente> listarClientes() {
        return repository.findAll();
    }

    public Cliente buscarPorDniCuit(String dniCuit) {
        return repository.findByDniCuit(dniCuit)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe cliente con DNI/CUIT " + dniCuit));
    }

    public void activar(String dniCuit, String usuario) {
        Cliente cliente = buscarPorDniCuit(dniCuit);
        String anterior = cliente.getEstado().name();
        cliente.activar();
        repository.save(cliente);
        historial.registrar(TipoEntidad.CLIENTE, dniCuit, anterior,
                cliente.getEstado().name(), usuario);
    }

    public void inactivar(String dniCuit, String usuario) {
        Cliente cliente = buscarPorDniCuit(dniCuit);
        String anterior = cliente.getEstado().name();
        cliente.inactivar();
        repository.save(cliente);
        historial.registrar(TipoEntidad.CLIENTE, dniCuit, anterior,
                cliente.getEstado().name(), usuario);
    }

    public void agregarCredito(String dniCuit, double importe) {
        Cliente cliente = buscarPorDniCuit(dniCuit);
        cliente.agregarCredito(importe);
        repository.save(cliente);
    }

    public void agregarDescuento(String dniCuit, double porcentaje,
                                 LocalDate desde, LocalDate hasta) {
        Cliente cliente = buscarPorDniCuit(dniCuit);
        cliente.agregarDescuento(new DescuentoCliente(porcentaje, desde, hasta));
        repository.save(cliente);
    }

    public double obtenerDescuentoVigente(String dniCuit, LocalDate fecha) {
        return buscarPorDniCuit(dniCuit).obtenerDescuentoVigente(fecha);
    }
}
