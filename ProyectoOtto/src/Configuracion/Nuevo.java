/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author luisd
 */


public class Nuevo {

    public static void crearNuevaHoja(JDesktopPane escritorio) {
        // Crear una hoja (internal frame)
        JInternalFrame hoja = new JInternalFrame("Documento", true, true, true, true);
        hoja.setSize(650, 400);

        // Configurar el JTextArea
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Scroll
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        // Botón para procesar el texto
        JButton btnProcesar = new JButton("Construir Autómata");
        btnProcesar.addActionListener(e -> {
            String texto = textArea.getText();
            procesarTexto(texto, escritorio); // <- Enviar al método de parseo
        });

        // Panel inferior con botón
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnProcesar);

        // Añadir todo al InternalFrame
        hoja.getContentPane().add(scrollPane, BorderLayout.CENTER);
        hoja.getContentPane().add(panelBotones, BorderLayout.SOUTH);

        escritorio.add(hoja, JLayeredPane.DEFAULT_LAYER);
        hoja.setVisible(true);
        PanelLateral.mostrar(escritorio);
    }

    static void procesarTexto(String texto, JDesktopPane escritorio) {
        String[] lineas = texto.split("\n");
        String estadoInicial = "";
        List<String> estadosAceptacion = new ArrayList<>();
        List<String> simbolos = new ArrayList<>();
        List<String> estados = new ArrayList<>();
        List<String[]> transiciones = new ArrayList<>();
        StringBuilder cadenas = new StringBuilder();

        boolean enTransiciones = false;
        boolean enCadenas = false;

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }

            if (linea.equalsIgnoreCase("Transiciones:")) {
                enTransiciones = true;
                enCadenas = false;
                continue;
            }

            if (linea.equalsIgnoreCase("Cadenas a analizar:")) {
                enTransiciones = false;
                enCadenas = true;
                continue;
            }

            if (linea.startsWith("Estados:")) {
                String[] parts = linea.replace("Estados:", "").trim().split(",");
                for (String part : parts) {
                    estados.add(part.trim());
                }
            } else if (linea.startsWith("Simbolos:")) {
                String[] parts = linea.replace("Simbolos:", "").trim().split(",");
                for (String part : parts) {
                    simbolos.add(part.trim());
                }
            } else if (linea.startsWith("Estado inicial:")) {
                estadoInicial = linea.replace("Estado inicial:", "").trim();
            } else if (linea.startsWith("Estados de aceptación:")) {
                String[] parts = linea.replace("Estados de aceptación:", "").trim().split(",");
                for (String part : parts) {
                    estadosAceptacion.add(part.trim());
                }
            } else if (enTransiciones) {
                String[] parts = linea.split(",");
                if (parts.length == simbolos.size()) {
                    transiciones.add(parts); // formato reducido, sin origen
                } else if (parts.length == 3) {
                    // formato completo Q0,0,Q1
                    transiciones.add(parts);
                }
            } else if (enCadenas) {
                if (cadenas.length() > 0) {
                    cadenas.append("\n");
                }
                cadenas.append(linea);
            }
        }

        // ✅ Reconstruir transiciones con el estado origen implícito (formato reducido)
        List<String[]> transicionesCompletas = new ArrayList<>();
        for (int i = 0; i < transiciones.size() && i < estados.size(); i++) {
            String origen = estados.get(i);
            String[] destinos = transiciones.get(i);
            for (int j = 0; j < destinos.length && j < simbolos.size(); j++) {
                String simbolo = simbolos.get(j);
                transicionesCompletas.add(new String[]{origen, simbolo, destinos[j].trim()});
            }
        }
        // ✅ Llenar las tablas en el panel lateral (si lo estás usando)
        PanelLateral.llenarEstados(estados.toArray(new String[0]));
        PanelLateral.llenarSimbolos(simbolos.toArray(new String[0]));
        PanelLateral.setEstadoInicial(estadoInicial);
        PanelLateral.llenarAceptacion(estadosAceptacion.toArray(new String[0]));
        PanelLateral.llenarTransiciones(transicionesCompletas);
        PanelLateral.setCadenasATestear(cadenas.toString());

        System.out.println("🧩 Texto de cadenas detectado:\n" + cadenas);

        // ✅ CORRECCIÓN: Actualizar el panel gráfico directamente
        if (PanelLateral.getPanelGrafica() != null) {
            // Pasar los datos procesados al panel gráfico
            PanelLateral.getPanelGrafica().actualizarDiagrama(
                    estados,
                    estadoInicial,
                    estadosAceptacion,
                    transicionesCompletas,
                    simbolos,
                    cadenas.toString()
            );
        } else {
            // Si el panel gráfico no está disponible, mostrar mensaje de advertencia
            System.out.println("Advertencia: Panel gráfico no disponible");
        }

        JOptionPane.showMessageDialog(null, "Autómata y cadenas cargadas correctamente ✅");

    }
}