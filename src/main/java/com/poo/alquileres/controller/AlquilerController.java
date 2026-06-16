package com.poo.alquileres.controller;

import com.poo.alquileres.model.Alquiler;
import com.poo.alquileres.model.AlquilerComun;
import com.poo.alquileres.model.AlquilerCorporativo;
import com.poo.alquileres.model.AlquilerMasivo;
import com.poo.alquileres.model.Cliente;
import com.poo.alquileres.model.DetalleAlquiler;
import com.poo.alquileres.model.Equipo;
import com.poo.alquileres.model.HistorialCambioEstado;
import com.poo.alquileres.model.Pago;
import com.poo.alquileres.model.enums.EstadoAlquiler;
import com.poo.alquileres.model.enums.EstadoPago;
import com.poo.alquileres.model.enums.MedioPago;
import com.poo.alquileres.model.enums.TipoAlquiler;
import com.poo.alquileres.model.enums.TipoEntidad;
import com.poo.alquileres.persistence.repository.AlquilerRepository;
import com.poo.alquileres.persistence.repository.EquipoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final HistorialController historialController = HistorialController.getInstance();

    // Recargos configurables (atributos que el diagrama de clases ubicaba en Sistema).
    private double recargoMasivo = AlquilerMasivo.RECARGO_MASIVO;
    private double recargoCorporativo = AlquilerCorporativo.RECARGO_CORPORATIVO;

    private AlquilerController() {
    }

    public static synchronized AlquilerController getInstance() {
        if (instance == null) {
            instance = new AlquilerController();
        }
        return instance;
    }

    /**
     * Crea un alquiler en estado INGRESADO (UC3): ubica el cliente, instancia la subclase
     * según el tipo, recorre los items reservando stock y armando los detalles, y registra
     * la auditoría.
     */
    public Alquiler solicitarAlquiler(String dniCuit, List<ItemSolicitado> itemsSolicitados,
                                      LocalDate fechaEvento, int cantidadDias,
                                      TipoAlquiler tipoAlquiler, String usuario) {
        Cliente cliente = null;
        for (Cliente clienteActual : clienteController.listarClientes()) {
            if (clienteActual.coincideDniCuit(dniCuit)) {
                cliente = clienteActual;
                break;
            }
        }
        if (cliente == null) {
            throw new IllegalArgumentException("No existe cliente con DNI/CUIT " + dniCuit);
        }
        if (!cliente.estaActivo()) {
            throw new IllegalStateException("El cliente " + dniCuit + " está inactivo.");
        }
        if (itemsSolicitados == null || itemsSolicitados.isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos un equipo.");
        }
        if (cantidadDias <= 0) {
            throw new IllegalArgumentException("La cantidad de días debe ser positiva.");
        }

        Alquiler alquiler = crearSegunTipo(tipoAlquiler, cliente, fechaEvento, cantidadDias);
        alquiler.setId(repository.nextId());

        for (ItemSolicitado item : itemsSolicitados) {
            Equipo equipo = null;
            for (Equipo equipoActual : equipoRepository.findAll()) {
                if (equipoActual.coincideCodigo(item.codigoEquipo())) {
                    equipo = equipoActual;
                    break;
                }
            }
            if (equipo == null) {
                throw new IllegalArgumentException(
                        "No existe equipo con código " + item.codigoEquipo());
            }

            if (equipo.estaDisponible(item.cantidad())) {
                DetalleAlquiler detalle = new DetalleAlquiler(
                        equipo, item.cantidad(), equipo.getValorDiario());
                alquiler.agregarDetalle(detalle);
                equipo.reservarStock(item.cantidad());
                equipoRepository.save(equipo);
            } else {
                throw new IllegalStateException(
                        "Equipo no disponible para la cantidad pedida: " + equipo.getNombre());
            }
        }

        alquiler.cambiarEstado(EstadoAlquiler.INGRESADO);

        double porcentajeDescuento = cliente.obtenerDescuentoVigente(LocalDate.now());
        alquiler.calcularImportePendiente(porcentajeDescuento);

        repository.save(alquiler);

        HistorialCambioEstado historial = new HistorialCambioEstado(
                LocalDateTime.now(), "-", alquiler.getEstado().name(),
                TipoEntidad.ALQUILER, String.valueOf(alquiler.getId()), usuario);
        historial.asociarAlquiler(alquiler);
        historialController.agregarHistorial(historial);
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
        historialController.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
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
     * Finaliza el alquiler (UC4): ubica el alquiler, controla la devolución liberando el
     * stock de cada detalle, recalcula los importes con el descuento vigente a la fecha del
     * evento, finaliza y devuelve el importe pendiente.
     */
    public double finalizarAlquiler(int idAlquiler, String usuario) {
        Alquiler alquiler = null;
        for (Alquiler alquilerActual : repository.findAll()) {
            if (alquilerActual.coincideId(idAlquiler)) {
                alquiler = alquilerActual;
                break;
            }
        }
        if (alquiler == null) {
            throw new IllegalArgumentException("No existe alquiler con id " + idAlquiler);
        }
        String anterior = alquiler.getEstado().name();

        Cliente cliente = alquiler.obtenerCliente();
        List<DetalleAlquiler> detalles = alquiler.obtenerDetalles();

        for (DetalleAlquiler detalleActual : detalles) {
            Equipo equipo = detalleActual.obtenerEquipo();
            int cantidad = detalleActual.obtenerCantidad();
            if (equipo != null) {
                equipoRepository.findByCodigo(equipo.getCodigo()).ifPresent(persistido -> {
                    persistido.liberarStock(cantidad);
                    equipoRepository.save(persistido);
                });
            }
        }

        double porcentajeDescuento = (cliente != null)
                ? cliente.obtenerDescuentoVigente(alquiler.getFechaEvento())
                : 0.0;
        alquiler.calcularSubtotal();
        alquiler.obtenerPorcentajeRecargo();
        alquiler.calcularImporteTotal(porcentajeDescuento);
        double importePendiente = alquiler.calcularImportePendiente(porcentajeDescuento);

        alquiler.finalizar();

        repository.save(alquiler);
        HistorialCambioEstado historial = new HistorialCambioEstado(
                LocalDateTime.now(), anterior, alquiler.getEstado().name(),
                TipoEntidad.ALQUILER, String.valueOf(idAlquiler), usuario);
        historial.asociarAlquiler(alquiler);
        historialController.agregarHistorial(historial);
        return importePendiente;
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
        historialController.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
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
                .filter(a -> a.getCliente() != null && a.getCliente().coincideDniCuit(dniCuit))
                .filter(a -> a.getEstado() == EstadoAlquiler.CONFIRMADO)
                .toList();
    }

    /**
     * Total recaudado por pagos confirmados dentro del rango de fechas (inclusive).
     */
    public double totalRecaudado(LocalDate fechaDesde, LocalDate fechaHasta) {
        return repository.findAll().stream()
                .flatMap(a -> a.getPagos().stream())
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
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
        double descuento = descuentoVigente(alquiler, LocalDate.now());
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
        historialController.registrar(TipoEntidad.ALQUILER, String.valueOf(idAlquiler),
                anterior, alquiler.getEstado().name(), usuario);
        return alquiler;
    }

    private double recalcular(Alquiler alquiler) {
        return alquiler.calcularImportePendiente(descuentoVigente(alquiler, LocalDate.now()));
    }

    /** Descuento vigente del cliente del alquiler en la fecha indicada (0 si no aplica). */
    private double descuentoVigente(Alquiler alquiler, LocalDate fecha) {
        Cliente cliente = alquiler.getCliente();
        return cliente != null ? cliente.obtenerDescuentoVigente(fecha) : 0.0;
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

    private Alquiler crearSegunTipo(TipoAlquiler tipo, Cliente cliente,
                                    LocalDate fechaEvento, int dias) {
        return switch (tipo) {
            case MASIVO -> new AlquilerMasivo(cliente, fechaEvento, dias, recargoMasivo);
            case CORPORATIVO -> new AlquilerCorporativo(cliente, fechaEvento, dias, recargoCorporativo);
            case COMUN -> new AlquilerComun(cliente, fechaEvento, dias, 0.0);
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
