/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

import vista.PanelConfi;
import java.awt.Dimension;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Vector;
import vista.Diagrama;

public class PanelLateral {

    private static JInternalFrame configFrame;
    private static boolean isMinimized = false;
    private static Dimension originalSize;
    private static PanelConfi panelConfi;
    private static GraficaDiagrama panelGrafica; // ✅ Nuevo panel para el gráfico

    public static void mostrar(JDesktopPane escritorio) {
        if (configFrame == null || configFrame.isClosed()) {
            // Crear el marco lateral sin botones de control
            configFrame = new JInternalFrame("Panel Lateral", false, false, false, false);

            // Crear contenedor principal con pestañas
            JTabbedPane pestañas = new JTabbedPane();

            // Crear y agregar panel de configuración
            panelConfi = new PanelConfi();
            pestañas.addTab("Configuración ⚙️", panelConfi);

            // Crear el Diagrama y el GraficaDiagrama
            Diagrama diagrama = new Diagrama();
            panelGrafica = new GraficaDiagrama(diagrama);

            pestañas.addTab("Gráfico 📊", panelGrafica);

            // Contenedor con el botón de minimizar
            JPanel contentPanel = new JPanel(new BorderLayout());
            JButton toggleButton = new JButton("—");
            toggleButton.addActionListener(e -> toggleMinimized(escritorio));

            contentPanel.add(toggleButton, BorderLayout.NORTH);
            contentPanel.add(pestañas, BorderLayout.CENTER);

            configFrame.getContentPane().add(contentPanel);
            configFrame.pack();
            configFrame.setVisible(true);

            escritorio.add(configFrame);
            originalSize = configFrame.getSize();
            ajustarPosicion(escritorio);
        } else {
            try {
                if (isMinimized) {
                    toggleMinimized(escritorio);
                }
                configFrame.setSelected(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ✅ Permite acceder al panel GraficaDiagrama desde fuera
    public static GraficaDiagrama getPanelGrafica() {
        return panelGrafica;
    }

    public static PanelConfi getPanelConfi() {
        return panelConfi;
    }

    private static void toggleMinimized(JDesktopPane escritorio) {
        if (isMinimized) {
            // Restaurar tamaño original
            configFrame.setSize(originalSize);
        } else {
            // Minimizar
            configFrame.setSize(30, configFrame.getHeight());
        }
        isMinimized = !isMinimized;
        ajustarPosicion(escritorio);
    }

    private static void ajustarPosicion(JDesktopPane escritorio) {
        Dimension desktopSize = escritorio.getSize();
        Dimension configSize = configFrame.getSize();

        // Lado derecho del escritorio
        configFrame.setLocation(desktopSize.width - configSize.width, 0);
        configFrame.setSize(configSize.width, desktopSize.height);
    }

    public static void cerrar() {
        if (configFrame != null) {
            configFrame.dispose();
            configFrame = null;
            isMinimized = false;
        }
    }

    public static void actualizarPosicion(JDesktopPane escritorio) {
        if (configFrame != null && !configFrame.isClosed()) {
            ajustarPosicion(escritorio);
        }
    }

    // ---------------- Métodos de carga de datos ----------------
    public static void llenarEstados(String[] estados) {
        if (panelConfi != null) {
            DefaultTableModel model = (DefaultTableModel) panelConfi.getEstadoDeAceptacionTable().getModel();
            model.setRowCount(0);
            for (int i = 0; i < estados.length; i++) {
                model.addRow(new Object[]{i + 1, estados[i].trim()});
            }
        }
    }

    public static void llenarSimbolos(String[] simbolos) {
        if (panelConfi != null) {
            DefaultTableModel model = (DefaultTableModel) panelConfi.getSimboloTable().getModel();
            model.setRowCount(0);
            for (int i = 0; i < simbolos.length; i++) {
                model.addRow(new Object[]{i + 1, simbolos[i].trim()});
            }
        }
    }

    public static void setEstadoInicial(String estadoInicial) {
        if (panelConfi != null) {
            panelConfi.getEstadoInicialField().setText(estadoInicial);
        }
    }

    public static void llenarAceptacion(String[] estadosAceptacion) {
        if (panelConfi != null) {
            DefaultTableModel model = (DefaultTableModel) panelConfi.getEstadoDeAceptacionTable().getModel();
            model.setRowCount(0);
            for (int i = 0; i < estadosAceptacion.length; i++) {
                model.addRow(new Object[]{i + 1, estadosAceptacion[i].trim()});
            }
        }
    }

    public static void llenarTransiciones(List<String[]> transiciones) {
        if (panelConfi != null) {
            DefaultTableModel model = (DefaultTableModel) panelConfi.getTransicionTable().getModel();
            model.setRowCount(0);

            // Extraer estados y símbolos únicos
            Set<String> estados = new LinkedHashSet<>();
            Set<String> simbolos = new LinkedHashSet<>();

            for (String[] t : transiciones) {
                if (t.length == 3) {
                    estados.add(t[0].trim());
                    simbolos.add(t[1].trim());
                    estados.add(t[2].trim());
                }
            }

            if (estados.isEmpty()) {
                return;
            }

            // Ordenar estados y símbolos
            List<String> listaEstados = new ArrayList<>(estados);
            listaEstados.sort(Comparator.comparing(a -> a.replaceAll("\\D", ""), Comparator.nullsLast(String::compareTo)));

            List<String> listaSimbolos = new ArrayList<>(simbolos);
            listaSimbolos.sort(Comparator.naturalOrder());

            Vector<String> columnas = new Vector<>();
            columnas.add("Estado");
            columnas.addAll(listaSimbolos);
            model.setColumnIdentifiers(columnas);

            Map<String, Map<String, String>> mapa = new LinkedHashMap<>();
            for (String estado : listaEstados) {
                mapa.put(estado, new LinkedHashMap<>());
            }

            for (String[] t : transiciones) {
                if (t.length == 3) {
                    String origen = t[0].trim();
                    String simbolo = t[1].trim();
                    String destino = t[2].trim();
                    mapa.get(origen).put(simbolo, destino);
                }
            }

            for (String estado : listaEstados) {
                Vector<String> fila = new Vector<>();
                fila.add(estado);
                Map<String, String> destinos = mapa.get(estado);
                for (String simbolo : listaSimbolos) {
                    fila.add(destinos.getOrDefault(simbolo, ""));
                }
                model.addRow(fila);
            }
        }
    }

    public static void setCadenasATestear(String cadenas) {
        if (panelConfi != null) {
            panelConfi.getCadenasATestear().setText(cadenas);
        }
    }
}
