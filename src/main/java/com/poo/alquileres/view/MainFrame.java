package com.poo.alquileres.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * Ventana principal. Agrupa los paneles de cada módulo en pestañas. No contiene lógica
 * de negocio ni una clase Sistema: cada panel habla directamente con su controller.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Sistema de Alquiler de Equipos - POO Parcial 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", new ClientePanel());
        tabs.addTab("Equipos", new EquipoPanel());
        tabs.addTab("Alquileres", new AlquilerPanel());
        add(tabs);
    }
}
