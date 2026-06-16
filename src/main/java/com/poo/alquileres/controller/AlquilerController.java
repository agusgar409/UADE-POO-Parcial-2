package com.poo.alquileres.controller;

import com.poo.alquileres.model.Alquiler;
import com.poo.alquileres.model.AlquilerComun;
import com.poo.alquileres.model.AlquilerCorporativo;
import com.poo.alquileres.model.AlquilerMasivo;
import com.poo.alquileres.model.Cliente;
import com.poo.alquileres.model.DetalleAlquiler;
import com.poo.alquileres.model.Equipo;
import com.poo.alquileres.model.Pago;
import com.poo.alquileres.model.enums.EstadoAlquiler;
import com.poo.alquileres.model.enums.MedioPago;
import com.poo.alquileres.model.enums.TipoAlquiler;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.persistence.repository.AlquilerRepository;
import com.poo.alquileres.persistence.repository.EquipoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller Singleton de alquileres. Orquesta el ciclo de vida completo: solicitud,
 * confirmación, preparación, entrega, finalización y cancelación. Coordina con los
 * repositorios de equipos y con la auditoría.
 */
public class AlquilerController {

    private static AlquilerController instance;

    private final AlquilerRepository repository = new AlquilerRepository();
    private final EquipoRepository equipoRepository = new EquipoRepository();
    private final ClienteController clienteController = ClienteController.getInstance();
    private final HistorialController historial = HistorialController.getInstance();

    private AlquilerController() {
    }

    public static synchronized AlquilerController getInstance() {
        if (instance == null) {
            instance = new AlquilerController();
        }
        return instance;
    }

    /**
     * Crea un alquiler en estado INGRESADO: valida cliente, reserva stock de cada item,
     * arma los detalles y calcula el importe total con el descuento vigente del cliente.
     */
    public Alquiler solicitarAlquiler(String dniCuit, List<ItemSolicitado> itemsSolicitados,
                                      LocalDate fechaEvento, int cantidadDias,
                                      TipoAlquiler tipoAlquiler, String usuario) {
        Cliente cliente = clienteController.buscarPorDniCuit(dniCuit);
        if (!cliente.estaActivo()) {
            throw new IllegalStateException("El cliente " + dniCuit + " está inactivo.");
        }
        if (itemsSolicitados == null || itemsSolicitados.isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos un equipo.");
        }
        if (cantidadDias <= 0) {
            throw new IllegalArgumentException("La cantidad de días debe ser positiva.");
        }

        Alquiler alquiler = crearSegunTipo(tipoAlquiler, repository.nextId(),
                fechaEvento, cantidadDias);
        alquiler.setClienteDniCuit(dniCuit);

        // Recorrer items solicitados: ubicar el equipo, validar disponibilidad y armar el detalle.
        for (ItemSolicitado item : itemsSolicitados) {
            Equipo equipo = equipoRepository.findByCodigo(item.codigoEquipo())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe equipo con código " + item.codigoEquipo()));

            if (equipo.estaDisponible(item.cantidad())) {
                DetalleAlquiler detalle = new DetalleAlquiler(equipo, item.cantidad());
                alquiler.agregarDetalle(detalle);
                equipo.reservarStock(item.cantidad());
                equipoRepository.save(equipo);
            } else {
                throw new IllegalStateException(
                        "Equipo no disponible para la cantidad pedida: " + equipo.getNombre());
            }
        }

        double descuento = cliente.obtenerDescuentoVigente(LocalDate.now());
        alquiler.calcularImportePendiente(descuento);

        repository.save(alquiler);
        historial.registrar(TipoEntidad.ALQUILER, String.valueOf(alquiler.getId()),
                "-", alquiler.getEstado().name(), usuario);
        return alquiler;
    }

    /**
     * Confirma el alquiler registrando la seña como un pago.
     */
    public Alquiler confirmarAlquiler(int idAlquiler, double importeSenia,
                                      MedioPago medioPago, String usuario) {
        Alquiler alquiler = buscarPorId(idAlquiler);
        String anterior = alquiler.getEstado().name();

        if (importeSenia > 0) {
            Pago pago = new Pago(siguienteIdPago(alquiler), importeSenia, medioPago, usuario);
            pago.confirmar();
            alquiler.registrarPago(pago);
            alquiler.registrarSenia(importeSenia);
        }
        alquiler.confirmar();
        recalcular(alquiler);

        repository.save(alquiler);
        historial.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
                anterior, alquiler.getEstado().name(), usuario);
        return alquiler;
    }

    public Alquiler prepararAlquiler(int idAlquiler, String usuario) {
        return transicion(idAlquiler, usuario, Alquiler::pasarAEnPreparacion);
    }

    public Alquiler entregarAlquiler(int idAlquiler, String usuario) {
        return transicion(idAlquiler, usuario, Alquiler::entregar);
    }

    /**
     * Finaliza el alquiler: controla la devolución liberando el stock de cada detalle,
     * recalcula los importes con el descuento vigente a la fecha del evento y devuelve el
     * importe pendiente que queda a cobrar.
     */
    public double finalizarAlquiler(int idAlquiler, String usuario) {
        Alquiler alquiler = buscarPorId(idAlquiler);
        String anterior = alquiler.getEstado().name();

        // Control de devolución: liberar el stock reservado por cada detalle.
        liberarStockDeDetalles(alquiler);

        // Recalcular importes con el descuento vigente a la fecha del evento.
        double descuento = descuentoVigente(alquiler, alquiler.getFechaEvento());
        alquiler.calcularImporteTotal(descuento);
        double pendiente = alquiler.calcularImportePendiente(descuento);

        alquiler.finalizar();

        repository.save(alquiler);
        historial.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
                anterior, alquiler.getEstado().name(), usuario);
        return pendiente;
    }

    /**
     * Cancela el alquiler y libera el stock reservado.
     */
    public Alquiler cancelarAlquiler(int idAlquiler, LocalDate fechaCancelacion, String usuario) {
        Alquiler alquiler = buscarPorId(idAlquiler);
        String anterior = alquiler.getEstado().name();

        alquiler.cancelar();
        liberarStockDeDetalles(alquiler);

        repository.save(alquiler);
        int horas = alquiler.calcularHorasAnticipacion(fechaCancelacion);
        historial.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
                anterior, alquiler.getEstado().name() + " (anticipación " + horas + "h)", usuario);
        return alquiler;
    }

    public List<Alquiler> listarAlquileres() {
        return repository.findAll();
    }

    public Alquiler buscarPorId(int idAlquiler) {
        return repository.findById(idAlquiler)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe alquiler con id " + idAlquiler));
    }

    public List<Alquiler> obtenerAlquileresConfirmados(String dniCuit) {
        return repository.findAll().stream()
                .filter(a -> dniCuit.equals(a.getClienteDniCuit()))
                .filter(a -> a.getEstado() == EstadoAlquiler.CONFIRMADO)
                .toList();
    }

    /**
     * Total recaudado por pagos confirmados dentro del rango de fechas (inclusive).
     */
    public double totalRecaudado(LocalDate fechaDesde, LocalDate fechaHasta) {
        return repository.findAll().stream()
                .flatMap(a -> a.getPagos().stream())
                .filter(p -> p.getEstado() == com.poo.alquileres.model.enums.EstadoPago.CONFIRMADO)
                .filter(p -> dentroDeRango(p.getFecha(), fechaDesde, fechaHasta))
                .mapToDouble(Pago::getImporte)
                .sum();
    }

    /**
     * Porcentaje neto aplicable al alquiler = recargo por tipo - descuento vigente del cliente.
     */
    public double obtenerRecargoDescuentoAplicable(int idAlquiler) {
        Alquiler alquiler = buscarPorId(idAlquiler);
        double recargo = alquiler.obtenerPorcentajeRecargo();
        double descuento = 0.0;
        if (alquiler.getClienteDniCuit() != null) {
            descuento = clienteController.obtenerDescuentoVigente(
                    alquiler.getClienteDniCuit(), LocalDate.now());
        }
        return recargo - descuento;
    }

    // ----- Helpers -----

    private interface TransicionEstado {
        void aplicar(Alquiler alquiler);
    }

    private Alquiler transicion(int idAlquiler, String usuario, TransicionEstado accion) {
        Alquiler alquiler = buscarPorId(idAlquiler);
        String anterior = alquiler.getEstado().name();
        accion.aplicar(alquiler);
        repository.save(alquiler);
        historial.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
                anterior, alquiler.getEstado().name(), usuario);
        return alquiler;
    }

    private double recalcular(Alquiler alquiler) {
        return alquiler.calcularImportePendiente(descuentoVigente(alquiler, LocalDate.now()));
    }

    /** Descuento vigente del cliente del alquiler en la fecha indicada (0 si no aplica). */
    private double descuentoVigente(Alquiler alquiler, LocalDate fecha) {
        if (alquiler.getClienteDniCuit() == null) {
            return 0.0;
        }
        return clienteController.obtenerDescuentoVigente(alquiler.getClienteDniCuit(), fecha);
    }

    /** Libera el stock reservado por cada detalle, persistiendo el equipo afectado. */
    private void liberarStockDeDetalles(Alquiler alquiler) {
        for (DetalleAlquiler detalle : alquiler.getDetalles()) {
            Equipo equipo = detalle.getEquipo();
            if (equipo != null) {
                equipoRepository.findByCodigo(equipo.getCodigo()).ifPresent(persistido -> {
                    persistido.liberarStock(detalle.getCantidad());
                    equipoRepository.save(persistido);
                });
            }
        }
    }

    private Alquiler crearSegunTipo(TipoAlquiler tipo, int id, LocalDate fechaEvento, int dias) {
        return switch (tipo) {
            case MASIVO -> new AlquilerMasivo(id, fechaEvento, dias);
            case CORPORATIVO -> new AlquilerCorporativo(id, fechaEvento, dias);
            case COMUN -> new AlquilerComun(id, fechaEvento, dias);
        };
    }

    private int siguienteIdPago(Alquiler alquiler) {
        return alquiler.getPagos().stream().mapToInt(Pago::getId).max().orElse(0) + 1;
    }

    private boolean dentroDeRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) {
            return false;
        }
        boolean despuesDesde = desde == null || !fecha.isBefore(desde);
        boolean antesHasta = hasta == null || !fecha.isAfter(hasta);
        return despuesDesde && antesHasta;
    }
}
