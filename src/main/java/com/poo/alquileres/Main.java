package com.poo.alquileres;

import com.poo.alquileres.view.MainFrame;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación Swing.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
