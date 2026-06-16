package com.poo.alquileres.view;

import com.poo.alquileres.controller.EquipoController;
import com.poo.alquileres.model.Equipo;
import com.poo.alquileres.model.enums.TipoEquipo;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

public class EquipoPanel extends JPanel {

    private static final String USUARIO = "ui";

    private final EquipoController controller = EquipoController.getInstance();

    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtDescripcion = new JTextField();
    private final JComboBox<TipoEquipo> cboTipo = new JComboBox<>(TipoEquipo.values());
    private final JTextField txtValorDiario = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JCheckBox chkInstalacion = new JCheckBox("Requiere instalación");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Código", "Nombre", "Tipo", "Estado", "Valor diario", "Stock", "Instalación"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(tableModel);

    private final JComboBox<Object> cboFiltroTipo =
            new JComboBox<>(construirOpcionesFiltro());
    private final JTextField txtFechaEvento =
            new JTextField(LocalDate.now().plusDays(7).toString());
    private final JTextField txtDias = new JTextField("1");

    public EquipoPanel() {
        setLayout(new BorderLayout(8, 8));
        add(construirFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(construirConsulta(), BorderLayout.SOUTH);
        refrescar();
    }

    private static Object[] construirOpcionesFiltro() {
        Object[] tipos = TipoEquipo.values();
        Object[] opciones = new Object[tipos.length + 1];
        opciones[0] = "(todos)";
        System.arraycopy(tipos, 0, opciones, 1, tipos.length);
        return opciones;
    }

    private JPanel construirConsulta() {
        JPanel consulta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        consulta.add(new JLabel("Tipo:"));
        consulta.add(cboFiltroTipo);
        consulta.add(new JLabel("Fecha evento:"));
        consulta.add(txtFechaEvento);
        consulta.add(new JLabel("Días:"));
        consulta.add(txtDias);

        JButton btnConsultar = new JButton("Consultar disponibles");
        btnConsultar.addActionListener(e -> consultarDisponibles());
        consulta.add(btnConsultar);
        JButton btnVerTodos = new JButton("Ver todos");
        btnVerTodos.addActionListener(e -> refrescar());
        consulta.add(btnVerTodos);
        return consulta;
    }

    private void consultarDisponibles() {
        try {
            Object seleccion = cboFiltroTipo.getSelectedItem();
            TipoEquipo tipoEvento = (seleccion instanceof TipoEquipo te) ? te : null;
            List<Equipo> disponibles = controller.consultarEquiposDisponibles(
                    LocalDate.parse(txtFechaEvento.getText().trim()),
                    Integer.parseInt(txtDias.getText().trim()),
                    tipoEvento);
            volcar(disponibles);
            if (disponibles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay equipos disponibles para ese filtro.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los días deben ser numéricos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel construirFormulario() {
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Código:"));
        form.add(txtCodigo);
        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        form.add(new JLabel("Descripción:"));
        form.add(txtDescripcion);
        form.add(new JLabel("Tipo:"));
        form.add(cboTipo);
        form.add(new JLabel("Valor diario:"));
        form.add(txtValorDiario);
        form.add(new JLabel("Stock inicial:"));
        form.add(txtStock);
        form.add(new JLabel(""));
        form.add(chkInstalacion);

        JButton btnRegistrar = new JButton("Registrar equipo");
        btnRegistrar.addActionListener(e -> registrar());
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescar());
        form.add(btnRegistrar);
        form.add(btnRefrescar);
        return form;
    }

    private void registrar() {
        try {
            controller.registrarEquipo(
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    txtDescripcion.getText().trim(),
                    (TipoEquipo) cboTipo.getSelectedItem(),
                    Double.parseDouble(txtValorDiario.getText().trim()),
                    Integer.parseInt(txtStock.getText().trim()),
                    chkInstalacion.isSelected(),
                    USUARIO);
            limpiar();
            refrescar();
            JOptionPane.showMessageDialog(this, "Equipo registrado.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor diario y stock deben ser numéricos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescar() {
        volcar(controller.listarEquipos());
    }

    private void volcar(List<Equipo> equipos) {
        tableModel.setRowCount(0);
        for (Equipo e : equipos) {
            tableModel.addRow(new Object[]{
                    e.getCodigo(), e.getNombre(), e.getTipoEquipo(), e.getEstado(),
                    e.getValorDiario(), e.getStockDisponible(),
                    e.isRequiereInstalacion() ? "Sí" : "No"});
        }
    }

    private void limpiar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtValorDiario.setText("");
        txtStock.setText("");
        chkInstalacion.setSelected(false);
    }
}
