package com.poo.alquileres.view;

import com.poo.alquileres.controller.ClienteController;
import com.poo.alquileres.model.Cliente;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Panel de alta y listado de clientes. Solo interactúa con ClienteController (Singleton).
 */
public class ClientePanel extends JPanel {

    private static final String USUARIO = "ui";

    private final ClienteController controller = ClienteController.getInstance();

    private final JTextField txtDni = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtDireccion = new JTextField();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"DNI/CUIT", "Nombre/Razón Social", "Teléfono", "Email", "Estado", "Crédito"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(tableModel);

    public ClientePanel() {
        setLayout(new BorderLayout(8, 8));
        add(construirFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        refrescar();
    }

    private JPanel construirFormulario() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new javax.swing.JLabel("DNI/CUIT:"));
        form.add(txtDni);
        form.add(new javax.swing.JLabel("Nombre / Razón Social:"));
        form.add(txtNombre);
        form.add(new javax.swing.JLabel("Teléfono:"));
        form.add(txtTelefono);
        form.add(new javax.swing.JLabel("Email:"));
        form.add(txtEmail);
        form.add(new javax.swing.JLabel("Dirección:"));
        form.add(txtDireccion);

        JButton btnRegistrar = new JButton("Registrar cliente");
        btnRegistrar.addActionListener(e -> registrar());
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescar());
        form.add(btnRegistrar);
        form.add(btnRefrescar);
        return form;
    }

    private void registrar() {
        try {
            controller.registrarCliente(
                    txtDni.getText().trim(),
                    txtNombre.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtEmail.getText().trim(),
                    txtDireccion.getText().trim(),
                    USUARIO);
            limpiar();
            refrescar();
            JOptionPane.showMessageDialog(this, "Cliente registrado.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescar() {
        tableModel.setRowCount(0);
        List<Cliente> clientes = controller.listarClientes();
        for (Cliente c : clientes) {
            tableModel.addRow(new Object[]{
                    c.getDniCuit(), c.getNombreRazonSocial(), c.getTelefono(),
                    c.getEmail(), c.getEstado(), c.getCreditoAFavor()});
        }
    }

    private void limpiar() {
        txtDni.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
    }
}
