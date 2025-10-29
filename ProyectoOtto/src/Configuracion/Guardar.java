/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class Guardar implements ActionListener {

    private JDesktopPane escritorio;
    private File archivoActual;
    private JInternalFrame frameActual;
    private JTextArea textArea;
    private Timer timerAutoGuardado;
    private Preferences preferencias;
    private boolean autoGuardadoActivo;
    private int intervaloAutoGuardado; // en minutos
    private Map<JInternalFrame, File> archivosPorFrame;

    // Constructor
    public Guardar(JDesktopPane escritorio) {
        this.escritorio = escritorio;
        this.preferencias = Preferences.userNodeForPackage(this.getClass());
        this.archivosPorFrame = new HashMap<>();
        cargarConfiguracion();
        if (autoGuardadoActivo) {
            iniciarAutoGuardado();
        }
    }

    // Cargar configuración desde preferencias
    private void cargarConfiguracion() {
        this.intervaloAutoGuardado = preferencias.getInt("intervaloAutoGuardado", 5);
        this.autoGuardadoActivo = preferencias.getBoolean("autoGuardadoActivo", true);
    }

    // Configurar auto-guardado
    public void configurarAutoGuardado(boolean activar, int intervaloMinutos) {
        this.autoGuardadoActivo = activar;
        this.intervaloAutoGuardado = intervaloMinutos;

        preferencias.putBoolean("autoGuardadoActivo", activar);
        preferencias.putInt("intervaloAutoGuardado", intervaloMinutos);

        if (activar) {
            iniciarAutoGuardado();
        } else {
            detenerAutoGuardado();
        }
    }

    // Iniciar temporizador de auto-guardado
    public void iniciarAutoGuardado() {
        detenerAutoGuardado();
        int intervaloMs = intervaloAutoGuardado * 60 * 1000;
        timerAutoGuardado = new Timer(intervaloMs, this);
        timerAutoGuardado.setInitialDelay(intervaloMs);
        timerAutoGuardado.start();
    }

    // Detener auto-guardado
    public void detenerAutoGuardado() {
        if (timerAutoGuardado != null && timerAutoGuardado.isRunning()) {
            timerAutoGuardado.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timerAutoGuardado) {
            autoGuardar();
        }
    }

    private void autoGuardar() {
        obtenerFrameYTextArea();
        if (archivoActual != null && frameActual != null && textArea != null) {
            guardarDirectamente();
            mostrarMensajeAutoGuardado();
        }
    }

    public void guardarHoja() {
        obtenerFrameYTextArea();
        if (frameActual == null || textArea == null) {
            return;
        }

        archivoActual = archivosPorFrame.get(frameActual);
        if (archivoActual != null) {
            guardarDirectamente();
        } else {
            pedirUbicacionYGuardar();
        }
    }

    public void guardarComo() {
        obtenerFrameYTextArea();
        if (frameActual == null || textArea == null) {
            return;
        }

        pedirUbicacionYGuardar();
    }

    private void obtenerFrameYTextArea() {
        frameActual = escritorio.getSelectedFrame();
        if (frameActual == null) {
            mostrarError("No hay documento seleccionado");
            return;
        }

        textArea = buscarTextArea(frameActual);
        if (textArea == null) {
            mostrarError("No se encontró el área de texto");
        }
    }

    private void pedirUbicacionYGuardar() {
        JFileChooser fileChooser = configurarFileChooser();
        int userSelection = fileChooser.showSaveDialog(escritorio);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            archivoActual = fileChooser.getSelectedFile();
            String extension = obtenerExtension(fileChooser.getFileFilter());

            if (!archivoActual.getName().toLowerCase().endsWith(extension)) {
                archivoActual = new File(archivoActual.getAbsolutePath() + extension);
            }

            guardarDirectamente();
            archivosPorFrame.put(frameActual, archivoActual);
            actualizarTituloFrame();
        }
    }

    private void guardarDirectamente() {
        try {
            Files.write(archivoActual.toPath(), textArea.getText().getBytes(StandardCharsets.UTF_8));
            actualizarTituloFrame();
        } catch (IOException e) {
            mostrarError("Error al guardar: " + e.getMessage());
            archivosPorFrame.remove(frameActual);
            archivoActual = null;
        }
    }


private void actualizarTituloFrame() {
        if (archivoActual != null && frameActual != null) {
            String nombreArchivo = archivoActual.getName();
            String tituloActual = frameActual.getTitle();

            if (tituloActual == null || !tituloActual.contains(nombreArchivo)) {
                int lastDash = tituloActual != null ? tituloActual.lastIndexOf(" - ") : -1;
                String baseTitle = lastDash != -1 ? tituloActual.substring(0, lastDash)
                        : (tituloActual != null ? tituloActual : "Documento");

                frameActual.setTitle(baseTitle + " - " + nombreArchivo);
            }
        }
    }

    private JFileChooser configurarFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar documento");

        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Texto (.txt)", "txt");
        FileNameExtensionFilter mdFilter = new FileNameExtensionFilter("Markdown (.md)", "md");
        FileNameExtensionFilter htmlFilter = new FileNameExtensionFilter("HTML (.html)", "html");
        FileNameExtensionFilter javaFilter = new FileNameExtensionFilter("Java (.java)", "java");

        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.addChoosableFileFilter(mdFilter);
        fileChooser.addChoosableFileFilter(htmlFilter);
        fileChooser.addChoosableFileFilter(javaFilter);
        fileChooser.setFileFilter(txtFilter);

        if (archivoActual != null) {
            fileChooser.setCurrentDirectory(archivoActual.getParentFile());
            fileChooser.setSelectedFile(archivoActual);
        }

        return fileChooser;
    }

    private String obtenerExtension(javax.swing.filechooser.FileFilter filter) {
        String description = filter.getDescription();
        if (description.contains(".txt")) {
            return ".txt";
        }
        if (description.contains(".md")) {
            return ".md";
        }
        if (description.contains(".html")) {
            return ".html";
        }
        if (description.contains(".java")) {
            return ".java";
        }
        return ".txt";
    }

    private JTextArea buscarTextArea(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextArea) {
                return (JTextArea) comp;
            }
            if (comp instanceof Container) {
                JTextArea found = buscarTextArea((Container) comp);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(escritorio, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarMensajeAutoGuardado() {
        if (archivoActual != null) {
            JOptionPane.showMessageDialog(escritorio,
                    "Documento auto-guardado en:\n" + archivoActual.getPath(),
                    "Auto-Guardado", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean isAutoGuardadoActivo() {
        return autoGuardadoActivo;
    }

    public int getIntervaloAutoGuardado() {
        return intervaloAutoGuardado;
    }

    public File getArchivoActual() {
        return archivoActual;

}

    public static class ConfigAutoGuardadoDialog extends JDialog {

    private JCheckBox chkActivar;
    private JSpinner spnIntervalo;
    private JButton btnAceptar;
    private Guardar gestorGuardado;

    public ConfigAutoGuardadoDialog(JFrame parent, Guardar gestorGuardado) {
        super(parent, "Configurar Auto-Guardado", true);
        this.gestorGuardado = gestorGuardado;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setSize(350, 150);
        setLocationRelativeTo(getParent());

        chkActivar = new JCheckBox("Activar Auto-Guardado");
        chkActivar.setSelected(gestorGuardado.isAutoGuardadoActivo());

        spnIntervalo = new JSpinner(new SpinnerNumberModel(
                gestorGuardado.getIntervaloAutoGuardado(), 1, 60, 1));
        JLabel lblIntervalo = new JLabel("Intervalo (minutos):");

        btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        add(chkActivar);
        add(new JLabel());
        add(lblIntervalo);
        add(spnIntervalo);
        add(btnAceptar);
        add(btnCancelar);

        btnAceptar.addActionListener(this::guardarConfiguracion);
        btnCancelar.addActionListener(e -> dispose());
    }

    private void guardarConfiguracion(ActionEvent e) {
        boolean activar = chkActivar.isSelected();
        int intervalo = (int) spnIntervalo.getValue();
        gestorGuardado.configurarAutoGuardado(activar, intervalo);
        dispose();
    }
}
}
