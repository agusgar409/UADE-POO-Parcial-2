package com.poo.alquileres.view;

import com.poo.alquileres.controller.AlquilerController;
import com.poo.alquileres.controller.ItemSolicitado;
import com.poo.alquileres.model.Alquiler;
import com.poo.alquileres.model.enums.MedioPago;
import com.poo.alquileres.model.enums.TipoAlquiler;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel del ciclo de vida de alquileres. Solo interactúa con AlquilerController (Singleton).
 */
public class AlquilerPanel extends JPanel {

    private static final String USUARIO = "ui";

    private final AlquilerController controller = AlquilerController.getInstance();

    private final JTextField txtDni = new JTextField();
    private final JComboBox<TipoAlquiler> cboTipo = new JComboBox<>(TipoAlquiler.values());
    private final JTextField txtFechaEvento = new JTextField(LocalDate.now().plusDays(7).toString());
    private final JTextField txtDias = new JTextField("1");
    private final JTextArea txtItems = new JTextArea(4, 20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Cliente", "Tipo", "Estado", "Recargo %", "Total", "Pendiente"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(tableModel);

    public AlquilerPanel() {
        setLayout(new BorderLayout(8, 8));
        add(construirFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(construirAcciones(), BorderLayout.SOUTH);
        refrescar();
    }

    private JPanel construirFormulario() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("DNI/CUIT cliente:"));
        form.add(txtDni);
        form.add(new JLabel("Tipo de alquiler:"));
        form.add(cboTipo);
        form.add(new JLabel("Fecha evento (yyyy-MM-dd):"));
        form.add(txtFechaEvento);
        form.add(new JLabel("Cantidad de días:"));
        form.add(txtDias);
        form.add(new JLabel("Items (una línea por equipo: codigo:cantidad):"));
        form.add(new JScrollPane(txtItems));

        JButton btnSolicitar = new JButton("Solicitar alquiler");
        btnSolicitar.addActionListener(e -> solicitar());
        form.add(btnSolicitar);
        JButton btnRecaudado = new JButton("Total recaudado (todo)");
        btnRecaudado.addActionListener(e -> mostrarRecaudado());
        form.add(btnRecaudado);
        return form;
    }

    private JPanel construirAcciones() {
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Confirmar", this::confirmar));
        acciones.add(boton("Preparar", id -> controller.prepararAlquiler(id, USUARIO)));
        acciones.add(boton("Entregar", id -> controller.entregarAlquiler(id, USUARIO)));
        acciones.add(boton("Finalizar", this::finalizar));
        acciones.add(boton("Cancelar", this::cancelar));
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescar());
        acciones.add(btnRefrescar);
        return acciones;
    }

    private interface AccionId {
        void ejecutar(int idAlquiler);
    }

    private JButton boton(String texto, AccionId accion) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> {
            Integer id = idSeleccionado();
            if (id == null) {
                return;
            }
            try {
                accion.ejecutar(id);
                refrescar();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return btn;
    }

    private Integer idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccioná un alquiler de la tabla.");
            return null;
        }
        return (Integer) tableModel.getValueAt(fila, 0);
    }

    private void solicitar() {
        try {
            List<ItemSolicitado> items = parsearItems();
            controller.solicitarAlquiler(
                    txtDni.getText().trim(),
                    items,
                    LocalDate.parse(txtFechaEvento.getText().trim()),
                    Integer.parseInt(txtDias.getText().trim()),
                    (TipoAlquiler) cboTipo.getSelectedItem(),
                    USUARIO);
            txtItems.setText("");
            refrescar();
            JOptionPane.showMessageDialog(this, "Alquiler solicitado.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad de días/cantidades deben ser numéricas.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<ItemSolicitado> parsearItems() {
        List<ItemSolicitado> items = new ArrayList<>();
        for (String linea : txtItems.getText().split("\\r?\\n")) {
            String l = linea.trim();
            if (l.isEmpty()) {
                continue;
            }
            String[] partes = l.split(":");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Formato de item inválido: '" + l
                        + "'. Use codigo:cantidad");
            }
            items.add(new ItemSolicitado(partes[0].trim(), Integer.parseInt(partes[1].trim())));
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar al menos un item.");
        }
        return items;
    }

    private void confirmar(int idAlquiler) {
        String seniaStr = JOptionPane.showInputDialog(this, "Importe de la seña:", "0");
        if (seniaStr == null) {
            return;
        }
        MedioPago medio = (MedioPago) JOptionPane.showInputDialog(this, "Medio de pago:",
                "Confirmar", JOptionPane.QUESTION_MESSAGE, null,
                MedioPago.values(), MedioPago.EFECTIVO);
        if (medio == null) {
            return;
        }
        controller.confirmarAlquiler(idAlquiler, Double.parseDouble(seniaStr.trim()), medio, USUARIO);
    }

    private void finalizar(int idAlquiler) {
        double pendiente = controller.finalizarAlquiler(idAlquiler, USUARIO);
        JOptionPane.showMessageDialog(this,
                "Alquiler finalizado. Importe pendiente a cobrar: " + pendiente);
    }

    private void cancelar(int idAlquiler) {
        controller.cancelarAlquiler(idAlquiler, LocalDate.now(), USUARIO);
    }

    private void mostrarRecaudado() {
        double total = controller.totalRecaudado(LocalDate.now().minusYears(1), LocalDate.now());
        JOptionPane.showMessageDialog(this,
                "Total recaudado (último año, pagos confirmados): " + total);
    }

    private void refrescar() {
        tableModel.setRowCount(0);
        for (Alquiler a : controller.listarAlquileres()) {
            String dni = a.getCliente() != null ? a.getCliente().getDniCuit() : "";
            tableModel.addRow(new Object[]{
                    a.getId(), dni,
                    a.getClass().getSimpleName().replace("Alquiler", ""),
                    a.getEstado(), a.getPorcentajeRecargoAplicado(),
                    a.getImporteTotal(), a.getImportePendiente()});
        }
    }
}
