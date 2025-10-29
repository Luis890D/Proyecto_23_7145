/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

/**
 *
 * @author luisd
 */
import vista.Diagrama;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;

public class GraficaDiagrama extends JPanel {

    private Diagrama diagrama;
    private List<String> estados;
    private String estadoInicial;
    private List<String> estadosAceptacion;
    private List<String[]> transiciones;
    private String estadoActual;

    public GraficaDiagrama() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Gráfico del autómata aparecerá aquí 🧩", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(label, BorderLayout.CENTER);
    }

    public GraficaDiagrama(Diagrama diagrama) {
        this();
        setDiagrama(diagrama);
    }

    public void setDiagrama(Diagrama diagrama) {
        this.diagrama = diagrama;
        removeAll();
        setLayout(new BorderLayout());

        if (diagrama != null) {
            add(diagrama, BorderLayout.CENTER);
        } else {
            JLabel label = new JLabel("No hay diagrama disponible ❌", SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    public Diagrama getDiagrama() {
        return diagrama;
    }

    // ============================================================
    // MÉTODOS DE PROCESAMIENTO DE CADENAS
    // ============================================================
    public void procesarCadenas(String texto) {
        if (diagrama == null) {
            JOptionPane.showMessageDialog(this, "⚠️ No hay diagrama configurado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        procesarCadenas(diagrama, texto);
    }

    public static void procesarCadenas(Diagrama diagrama, String texto) {
        if (diagrama == null) {
            return;
        }

        if (texto == null || texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ No hay cadenas a procesar.");
            return;
        }

        String[] lineas = texto.split("\n");
        DefaultTableModel modeloTblCadenas = (DefaultTableModel) diagrama.getTblCadenas().getModel();
        modeloTblCadenas.setRowCount(0);

        List<String> listaCadenas = new ArrayList<>();
        int contador = 1;

        for (String linea : lineas) {
            linea = linea.trim();
            if (!linea.isEmpty()) {
                listaCadenas.add(linea);
                modeloTblCadenas.addRow(new Object[]{contador, linea, "Pendiente"});
                contador++;
            }
        }

        if (listaCadenas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se encontraron cadenas.");
            return;
        }

        String primeraCadena = listaCadenas.get(0).replace(",", "");
        diagrama.getTxtVisualizarCadenda().setText(primeraCadena);

        DefaultTableModel modeloVisualizar = new DefaultTableModel();
        diagrama.getTblVisualizar().setModel(modeloVisualizar);

        String[] simbolos = primeraCadena.split("");
        for (int i = 0; i < simbolos.length; i++) {
            modeloVisualizar.addColumn("Pos " + (i + 1));
        }
        modeloVisualizar.addRow(simbolos);

        diagrama.setListaCadenas(listaCadenas);
        diagrama.setIndiceCadenaActual(0);

        JOptionPane.showMessageDialog(null, "✅ Cadenas cargadas correctamente.");
    }

    // ============================================================
    // CAMBIAR ENTRE CADENAS
    // ============================================================
    public static void cambiarCadena(Diagrama diagrama) {
        historialEstados.clear();

        if (diagrama == null) {
            return;
        }

        List<String> lista = diagrama.getListaCadenas();
        if (lista == null || lista.isEmpty()) {
            return;
        }

        int indiceActual = diagrama.getIndiceCadenaActual();
        indiceActual = (indiceActual + 1) % lista.size();

        diagrama.setIndiceCadenaActual(indiceActual);
        mostrarCadena(diagrama, lista.get(indiceActual), indiceActual);
    }

    public static void regresarCadena(Diagrama diagrama) {
        if (diagrama == null) {
            return;
        }

        List<String> lista = diagrama.getListaCadenas();
        if (lista == null || lista.isEmpty()) {
            return;
        }

        int indiceActual = diagrama.getIndiceCadenaActual();
        indiceActual = (indiceActual - 1 + lista.size()) % lista.size();

        diagrama.setIndiceCadenaActual(indiceActual);
        mostrarCadena(diagrama, lista.get(indiceActual), indiceActual);
    }

    private static void mostrarCadena(Diagrama diagrama, String cadena, int indiceActual) {
        cadena = cadena.replace(",", "");
        diagrama.getTxtVisualizarCadenda().setText(cadena);

        DefaultTableModel modeloVisualizar = new DefaultTableModel();
        diagrama.getTblVisualizar().setModel(modeloVisualizar);

        String[] simbolos = cadena.split("");
        for (int i = 0; i < simbolos.length; i++) {
            modeloVisualizar.addColumn("Pos " + (i + 1));
        }
        modeloVisualizar.addRow(simbolos);

        DefaultTableModel modeloTblCadenas = (DefaultTableModel) diagrama.getTblCadenas().getModel();
        for (int i = 0; i < modeloTblCadenas.getRowCount(); i++) {
            modeloTblCadenas.setValueAt(i == indiceActual ? "Actual" : "Pendiente", i, 2);
        }

        // ✅ Reiniciamos la posición al inicio (columna 0)
        diagrama.setIndicePosicionCadena(0);

        // ✅ Limpiamos historial de estados al cargar nueva cadena
        historialEstados.clear();
        GraficaDiagrama grafica = diagrama.getGraficaDiagrama();
        if (grafica != null) {
            grafica.estadoActual = grafica.estadoInicial;
            historialEstados.add(grafica.estadoInicial);
        }

        // ✅ Pintar inmediatamente la primera columna
        SwingUtilities.invokeLater(() -> {
            resaltarColumnaActual(diagrama);
            diagrama.getTblVisualizar().repaint();
        });

        // ✅ También actualizar el diagrama con el estado inicial en verde
        if (grafica != null) {
            String rutaTxt = generarArchivoGraphviz(
                    grafica.estados,
                    grafica.estadoInicial,
                    grafica.estadosAceptacion,
                    grafica.transiciones,
                    1,
                    grafica.estadoInicial
            );
            generarYMostrarAutomata(diagrama, rutaTxt, 1);
        }
    }

    // ============================================================
    // ACTUALIZAR SOLO COLORES DEL DIAGRAMA
    // ============================================================
    public static void actualizarColoresDiagrama(Diagrama diagrama) {
        if (diagrama == null) {
            return;
        }

        // Obtener la instancia de GraficaDiagrama del Diagrama
        GraficaDiagrama grafica = diagrama.getGraficaDiagrama();
        if (grafica == null) {
            return;
        }

        // Regenerar el diagrama con los mismos datos pero solo actualizando colores
        if (grafica.estados != null && grafica.estadoInicial != null
                && grafica.estadosAceptacion != null && grafica.transiciones != null) {

            int contador = 1; // Puedes ajustar este contador según necesites

            // Generar archivo Graphviz con los mismos datos
            String rutaTxt = generarArchivoGraphviz(
                    grafica.estados,
                    grafica.estadoInicial,
                    grafica.estadosAceptacion,
                    grafica.transiciones,
                    contador,
                    grafica.estadoActual // ✅ AHORA SE PINTARÁ EL ESTADO ACTUAL
            );

            // Mostrar el autómata actualizado
            generarYMostrarAutomata(diagrama, rutaTxt, contador);
        }
    }

    // ============================================================
    // RESALTAR POSICIÓN ACTUAL EN TABLA
    // ============================================================
    public static void resaltarColumnaActual(Diagrama diagrama) {
        JTable tabla = diagrama.getTblVisualizar();
        int columnaActiva = diagrama.getIndicePosicionCadena();

        if (tabla == null || tabla.getColumnCount() == 0) {
            return;
        }

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(column == columnaActiva ? Color.YELLOW : Color.WHITE);
                c.setForeground(Color.BLACK);
                return c;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tabla.repaint();
    }

    // ============================================================
    // GENERAR Y MOSTRAR AUTOMATA CON GRAPHVIZ
    // ============================================================
    public void actualizarDiagrama(
            List<String> estados,
            String estadoInicial,
            List<String> estadosAceptacion,
            List<String[]> transiciones,
            List<String> simbolos,
            String cadenas
    ) {
        if (diagrama == null) {
            JOptionPane.showMessageDialog(this, "⚠️ No hay diagrama cargado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.estados = estados;
        this.estadoInicial = estadoInicial;
        this.estadosAceptacion = estadosAceptacion;
        this.transiciones = transiciones;
        this.estadoActual = estadoInicial; // ✅ Se inicia en el estado inicial

        int contador = 1;
        String rutaTxt = generarArchivoGraphviz(estados, estadoInicial, estadosAceptacion, transiciones, contador, estadoInicial);
        generarYMostrarAutomata(diagrama, rutaTxt, contador);

        if (cadenas != null && !cadenas.trim().isEmpty()) {
            procesarCadenas(diagrama, cadenas);
        }
    }

    // ============================================================
    // GENERAR ARCHIVO GRAPHVIZ Y MOSTRAR AUTÓMATA
    // ============================================================
    public static String generarArchivoGraphviz(
            List<String> estados,
            String estadoInicial,
            List<String> estadosAceptacion,
            List<String[]> transiciones,
            int contador,
            String estadoActual
    ) {
        String rutaEscritorio = System.getProperty("user.home") + "\\Desktop";
        String carpetaAutomata = rutaEscritorio + "\\AutomataFinito";
        String rutaEntrada = carpetaAutomata + "\\entradaGraphviz" + contador + ".txt";

        File carpeta = new File(carpetaAutomata);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaEntrada))) {
            writer.println("digraph automata_finito_determinista {");
            writer.println("rankdir=LR;");
            writer.println("node [shape=circle fontname=\"Arial\"];");

            for (String estado : estados) {
                writer.println(estado + " [color=lightgray style=filled];");
            }

            if (estadoInicial != null && !estadoInicial.isEmpty()) {
                writer.println(estadoInicial + " [color=orange style=filled];");
            }

            for (String estadoFinal : estadosAceptacion) {
                writer.println(estadoFinal + " [shape=doublecircle color=deepskyblue style=filled];");
            }

            if (estadoActual != null && !estadoActual.isEmpty()) {
                writer.println(estadoActual + " [color=palegreen style=filled];");
            }

            for (String[] t : transiciones) {
                if (t.length == 3) {
                    writer.println(t[0] + " -> " + t[2] + " [label=\"" + t[1] + "\"];");
                }
            }

            writer.println("}");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al generar archivo Graphviz:\n" + e.getMessage());
        }

        return rutaEntrada;
    }

    public static void generarYMostrarAutomata(Diagrama diagrama, String rutaTxtEntrada, int contador) {
        try {
            String rutaDot = "D:\\U 2024 DAVID\\APP\\Graphviz\\bin\\dot.exe";
            String rutaEscritorio = System.getProperty("user.home") + "\\Desktop";
            String carpetaAutomata = rutaEscritorio + "\\AutomataFinito";
            String rutaSalida = carpetaAutomata + "\\salida" + contador + ".png";

            ProcessBuilder pb = new ProcessBuilder(rutaDot, "-Tpng", "-Gdpi=78", rutaTxtEntrada, "-o", rutaSalida);
            pb.redirectErrorStream(true);
            Process proceso = pb.start();
            proceso.waitFor();

            File imagen = new File(rutaSalida);
            if (!imagen.exists()) {
                JOptionPane.showMessageDialog(null, "❌ No se generó la imagen del autómata.");
                return;
            }

            JPanel panel = diagrama.getMiniDiagrama();
            panel.removeAll();
            panel.setPreferredSize(new Dimension(400, 300));

            ImageIcon icono = new ImageIcon(rutaSalida);
            Image imagenEscalada = icono.getImage().getScaledInstance(400, 300, Image.SCALE_SMOOTH);
            JLabel etiqueta = new JLabel(new ImageIcon(imagenEscalada));

            panel.setLayout(new BorderLayout());
            panel.add(etiqueta, BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al generar el autómata:\n" + e.getMessage());
        }
    }
    // ============================================================
    // MOVER AUTOMATA A LA SIGUIENTE POSICIÓN Y ACTUALIZAR DIAGRAMA
    // ============================================================

    // 🔹 Agrega este atributo en tu clase GraficaDiagrama (arriba)
    private static List<String> historialEstados = new ArrayList<>();

    public static void moverAutomata(Diagrama diagrama, boolean avanzar) {
        if (diagrama == null) {
            return;
        }
        GraficaDiagrama grafica = diagrama.getGraficaDiagrama();
        if (grafica == null) {
            return;
        }

        JTable tabla = diagrama.getTblVisualizar();
        if (tabla == null || tabla.getColumnCount() == 0) {
            return;
        }

        int pos = diagrama.getIndicePosicionCadena();
        int max = tabla.getColumnCount() - 1;

        if (historialEstados.isEmpty()) {
            historialEstados.add(grafica.estadoInicial);
        }

        DefaultTableModel modeloCadenas = (DefaultTableModel) diagrama.getTblCadenas().getModel();
        int indiceCadena = diagrama.getIndiceCadenaActual();

        if (avanzar) {
            if (pos > max) {
                return;
            }

            if (pos == 0 && !historialEstados.isEmpty()) {
                grafica.estadoActual = grafica.estadoInicial;
                historialEstados.clear();
                historialEstados.add(grafica.estadoInicial);
            }

            String simbolo = tabla.getValueAt(0, pos).toString();
            String nuevoEstado = grafica.estadoActual;

            if (grafica.transiciones != null) {
                for (String[] t : grafica.transiciones) {
                    if (t[0].equals(grafica.estadoActual) && t[1].equals(simbolo)) {
                        nuevoEstado = t[2];
                        break;
                    }
                }
            }

            grafica.estadoActual = nuevoEstado;
            if (historialEstados.size() > pos) {
                historialEstados.set(pos, nuevoEstado);
            } else {
                historialEstados.add(nuevoEstado);
            }

            // Redibujar autómata
            String rutaTxt = GraficaDiagrama.generarArchivoGraphviz(
                    grafica.estados,
                    grafica.estadoInicial,
                    grafica.estadosAceptacion,
                    grafica.transiciones,
                    1,
                    grafica.estadoActual
            );
            GraficaDiagrama.generarYMostrarAutomata(diagrama, rutaTxt, 1);

            if (pos < max) {
                diagrama.setIndicePosicionCadena(pos + 1);
                GraficaDiagrama.resaltarColumnaActual(diagrama);
            } else {
                // Verificar aceptación al final
                boolean aceptada = grafica.estadosAceptacion.contains(grafica.estadoActual);
                String resultado = aceptada ? "Aceptada" : "Rechazada   ";

                // 🔹 Actualizar columna Resultado
                modeloCadenas.setValueAt(resultado, indiceCadena, 3);

                String msg = aceptada
                        ? "✅ Cadena aceptada (terminó en estado de aceptación: " + grafica.estadoActual + ")"
                        : "❌ Cadena rechazada (terminó en estado: " + grafica.estadoActual + ")";
                JOptionPane.showMessageDialog(diagrama, msg, "Resultado", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            if (pos <= 0) {
                JOptionPane.showMessageDialog(diagrama, "🔙 Ya estás al inicio de la cadena.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            diagrama.setIndicePosicionCadena(pos - 1);
            GraficaDiagrama.resaltarColumnaActual(diagrama);

            if (pos - 1 < historialEstados.size()) {
                grafica.estadoActual = historialEstados.get(pos - 1);
            } else {
                grafica.estadoActual = grafica.estadoInicial;
            }

            String rutaTxt = GraficaDiagrama.generarArchivoGraphviz(
                    grafica.estados,
                    grafica.estadoInicial,
                    grafica.estadosAceptacion,
                    grafica.transiciones,
                    1,
                    grafica.estadoActual
            );
            GraficaDiagrama.generarYMostrarAutomata(diagrama, rutaTxt, 1);
        }
    }
// ============================================================
// PROCESAR TODAS LAS CADENAS AUTOMÁTICAMENTE
// ============================================================

    public static void procesarTodasLasCadenas(Diagrama diagrama) {
        if (diagrama == null) {
            JOptionPane.showMessageDialog(null, "⚠️ No hay diagrama configurado.");
            return;
        }

        GraficaDiagrama grafica = diagrama.getGraficaDiagrama();
        if (grafica == null) {
            JOptionPane.showMessageDialog(null, "⚠️ No hay autómata configurado.");
            return;
        }

        List<String> listaCadenas = diagrama.getListaCadenas();
        if (listaCadenas == null || listaCadenas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "⚠️ No hay cadenas para procesar.");
            return;
        }

        DefaultTableModel modeloTblCadenas = (DefaultTableModel) diagrama.getTblCadenas().getModel();

        // Limpiar resultados anteriores
        for (int i = 0; i < modeloTblCadenas.getRowCount(); i++) {
            modeloTblCadenas.setValueAt("", i, 3); // Limpiar columna Resultado
        }

        // Procesar cada cadena
        for (int i = 0; i < listaCadenas.size(); i++) {
            String cadena = listaCadenas.get(i).replace(",", "");
            String resultado = procesarCadenaCompleta(grafica, cadena, i);
            modeloTblCadenas.setValueAt(resultado, i, 3);
            modeloTblCadenas.setValueAt("Procesada", i, 2);
        }

        JOptionPane.showMessageDialog(null, "✅ Todas las cadenas han sido procesadas.");
    }

// ============================================================
// PROCESAR UNA CADENA COMPLETA Y DEVOLVER RESULTADO
// ============================================================
    private static String procesarCadenaCompleta(GraficaDiagrama grafica, String cadena, int indiceCadena) {
        if (grafica.transiciones == null || grafica.estadoInicial == null) {
            return "Error: Autómata no configurado";
        }

        // Reiniciar al estado inicial para cada cadena
        String estadoActual = grafica.estadoInicial;

        // Procesar cada símbolo de la cadena
        for (int i = 0; i < cadena.length(); i++) {
            String simbolo = String.valueOf(cadena.charAt(i));
            String nuevoEstado = null;

            // Buscar transición válida
            for (String[] transicion : grafica.transiciones) {
                if (transicion[0].equals(estadoActual) && transicion[1].equals(simbolo)) {
                    nuevoEstado = transicion[2];
                    break;
                }
            }

            if (nuevoEstado == null) {
                // No hay transición para el símbolo actual
                return "Rechazada (sin transición)";
            }

            estadoActual = nuevoEstado;
        }

        // Verificar si el estado final es de aceptación
        boolean aceptada = grafica.estadosAceptacion.contains(estadoActual);
        return aceptada ? "Aceptada" : "Rechazada";
    }

    // ============================================================
    // GETTERS Y SETTERS PARA LOS DATOS DEL AUTÓMATA
    // ============================================================
    public List<String> getEstados() {
        return estados;
    }

    public void setEstados(List<String> estados) {
        this.estados = estados;
    }

    public String getEstadoInicial() {
        return estadoInicial;
    }

    public void setEstadoInicial(String estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public List<String> getEstadosAceptacion() {
        return estadosAceptacion;
    }

    public void setEstadosAceptacion(List<String> estadosAceptacion) {
        this.estadosAceptacion = estadosAceptacion;
    }

    public List<String[]> getTransiciones() {
        return transiciones;
    }

    public void setTransiciones(List<String[]> transiciones) {
        this.transiciones = transiciones;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(String estadoActual) {
        this.estadoActual = estadoActual;
    }
}
