/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import vista.MenuPrincipal;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vista.PanelConfi;

public class Abrir {

    private JDesktopPane escritorio;
    private MenuPrincipal menuPrincipal;
    private PanelConfi panelConfi;

    public Abrir(JDesktopPane escritorio, MenuPrincipal menuPrincipal, PanelConfi panelConfi) {
        this.escritorio = escritorio;
        this.menuPrincipal = menuPrincipal;
        this.panelConfi = panelConfi;
    }

    public void abrirArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir documento de texto");

        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Archivos de texto (.txt)", "txt");
        fileChooser.setFileFilter(txtFilter);

        int resultado = fileChooser.showOpenDialog(escritorio);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            cargarContenidoArchivo(archivoSeleccionado);
        }
    }

    private void cargarContenidoArchivo(File archivo) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {

            StringBuilder contenido = new StringBuilder();
            String linea;

            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append("\n");
            }

            // ✅ Mostrar el contenido con botón para construir
            mostrarHojaEnEscritorio(archivo.getName(), contenido.toString(), archivo);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(escritorio,
                    "Error al leer el archivo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarHojaEnEscritorio(String titulo, String contenido, File archivo) {
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);

        JTextArea textArea = new JTextArea(contenido);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton btnConstruir = new JButton("Construir Autómata");
        btnConstruir.addActionListener(e -> {
            String texto = textArea.getText();
            procesarTextoDesdeAbrir(texto);
        });

        JPanel panelBoton = new JPanel();
        panelBoton.add(btnConstruir);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(panelBoton, BorderLayout.SOUTH);

        frame.setSize(650, 400);
        centrarFrame(frame);
        frame.setVisible(true);
        escritorio.add(frame);

        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            System.err.println("Error al seleccionar frame: " + e.getMessage());
        }

        if (menuPrincipal != null) {
            menuPrincipal.registrarHoja(frame, textArea, archivo);
        }
    }

    private void centrarFrame(JInternalFrame frame) {
        Dimension desktopSize = escritorio.getSize();
        Dimension frameSize = frame.getSize();
        frame.setLocation(
                (desktopSize.width - frameSize.width) / 2,
                (desktopSize.height - frameSize.height) / 2
        );
    }

    // ✅ Ahora también genera el autómata gráfico igual que "Nuevo"
    private void procesarTextoDesdeAbrir(String texto) {
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
                    transiciones.add(parts); // formato reducido
                } else if (parts.length == 3) {
                    transiciones.add(parts); // formato completo
                }
            } else if (enCadenas) {
                if (cadenas.length() > 0) {
                    cadenas.append("\n");
                }
                cadenas.append(linea);
            }
        }

        // ✅ Reconstruir transiciones
        List<String[]> transicionesCompletas = new ArrayList<>();
        for (int i = 0; i < transiciones.size() && i < estados.size(); i++) {
            String origen = estados.get(i);
            String[] destinos = transiciones.get(i);
            for (int j = 0; j < destinos.length && j < simbolos.size(); j++) {
                String simbolo = simbolos.get(j);
                transicionesCompletas.add(new String[]{origen, simbolo, destinos[j].trim()});
            }
        }

        // ✅ Actualizar panel lateral
        PanelLateral.llenarEstados(estados.toArray(new String[0]));
        PanelLateral.llenarSimbolos(simbolos.toArray(new String[0]));
        PanelLateral.setEstadoInicial(estadoInicial);
        PanelLateral.llenarAceptacion(estadosAceptacion.toArray(new String[0]));
        PanelLateral.llenarTransiciones(transicionesCompletas);
        PanelLateral.setCadenasATestear(cadenas.toString());

        // ✅ Mostrar autómata en el panel gráfico
        if (PanelLateral.getPanelGrafica() != null) {
            PanelLateral.getPanelGrafica().actualizarDiagrama(
                    estados,
                    estadoInicial,
                    estadosAceptacion,
                    transicionesCompletas,
                    simbolos,
                    cadenas.toString()
            );
        } else {
            System.out.println("⚠️ Panel gráfico no disponible.");
        }

        JOptionPane.showMessageDialog(null, "Autómata y cadenas cargadas correctamente ✅");
    }
}
