/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

/**
 *
 * @author luisd
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Ejemplos {
    
    private static List<String[]> ejemplos = new ArrayList<>();
    
    static {
        // Inicializar los 3 ejemplos
        inicializarEjemplos();
    }
    
    private static void inicializarEjemplos() {
        // Ejemplo 1
        String[] ejemplo1 = {
            "Simbolos: 0,1,2",
            "Estados: Q0,Q1,Q2,Q3",
            "Estado inicial: Q0",
            "Estados de aceptación: Q3",
            "Transiciones:",
            "Q1,Q2,Q0",
            "Q2,Q3,Q1",
            "Q3,Q0,Q2",
            "Q0,Q1,Q3",
            "Cadenas a analizar:",
            "0,1,2",
            "1,0,2,2",
            "0,2,1,0,2",
            "2,0,1,1"
        };
        
        // Ejemplo 2
        String[] ejemplo2 = {
            "Simbolos: 0,1",
            "Estados: A,B",
            "Estado inicial: A",
            "Estados de aceptación: A",
            "Transiciones:",
            "A,B",
            "B,A",
            "Cadenas a analizar:",
            "1,0,1,0",
            "1,1,0",
            "0,0,1,1",
            "1,1,1,1,0"
        };
        
        // Ejemplo 3
        String[] ejemplo3 = {
            "Simbolos: a,b,c",
            "Estados: Q0,Q1,Q2,Q3,Q4",
            "Estado inicial: Q0",
            "Estados de aceptación: Q4",
            "Transiciones:",
            "Q1,Q0,Q2",
            "Q2,Q3,Q4",
            "Q3,Q4,Q0",
            "Q4,Q4,Q4",
            "Q0,Q1,Q2",
            "Cadenas a analizar:",
            "a,b,c,a,b",
            "a,c,c,b",
            "b,b,a,c,b",
            "c,a,b,b",
            "a,a,a,b"
        };
        
        ejemplos.add(ejemplo1);
        ejemplos.add(ejemplo2);
        ejemplos.add(ejemplo3);
    }
    
    // ✅ MÉTODO PÚBLICO ESTÁTICO para cargar ejemplos directamente
    public static void cargarEjemplo(int index, JDesktopPane escritorio) {
        if (index < 0 || index >= ejemplos.size()) {
            JOptionPane.showMessageDialog(null, "Ejemplo no encontrado");
            return;
        }
        
        // Crear nueva hoja
        JInternalFrame hoja = new JInternalFrame("Ejemplo " + (index + 1), true, true, true, true);
        hoja.setSize(650, 400);
        
        // Configurar el JTextArea
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Cargar el texto del ejemplo
        String[] lineasEjemplo = ejemplos.get(index);
        StringBuilder textoEjemplo = new StringBuilder();
        for (String linea : lineasEjemplo) {
            textoEjemplo.append(linea).append("\n");
        }
        textArea.setText(textoEjemplo.toString());
        
        // Scroll
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Botón para procesar el texto
        JButton btnProcesar = new JButton("Construir Autómata");
        btnProcesar.addActionListener(e -> {
            String texto = textArea.getText();
            Nuevo.procesarTexto(texto, escritorio);
        });
        
        // Panel inferior con botón
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnProcesar);
        
        // Añadir todo al InternalFrame
        hoja.getContentPane().add(scrollPane, BorderLayout.CENTER);
        hoja.getContentPane().add(panelBotones, BorderLayout.SOUTH);
        
        escritorio.add(hoja, JLayeredPane.DEFAULT_LAYER);
        hoja.setVisible(true);
        
        // Centrar la hoja en el escritorio
        centrarHoja(hoja, escritorio);
    }
    
    private static void centrarHoja(JInternalFrame hoja, JDesktopPane escritorio) {
        int x = (escritorio.getWidth() - hoja.getWidth()) / 2;
        int y = (escritorio.getHeight() - hoja.getHeight()) / 2;
        hoja.setLocation(x, y);
    }
    
    // El método crearMenuEjemplos lo puedes eliminar si no lo usas
}