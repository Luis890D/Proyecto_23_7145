/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vista;

import Configuracion.Operacion;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author luisd
 */
public class PanelConfi extends javax.swing.JPanel {

    private Operacion automata;
    private JTable TablaResultados;
    private JTextArea CadenasATestear;
    private JButton btnAnalizar; // Botón para analizar

    /**
     * Creates new form PanelConfi
     */
    public PanelConfi() {
        initComponents();

        // Inicializar JTextArea para ingresar cadenas
        CadenasATestear = new JTextArea(5, 20);
        JScrollPane scrollCadenas = new JScrollPane(CadenasATestear);
        scrollCadenas.setBorder(BorderFactory.createTitledBorder("Cadenas a analizar"));

        // Inicializar TablaResultados
        TablaResultados = new JTable(new DefaultTableModel(new Object[0][2], new String[]{"Cadena", "Resultado"}));
        JScrollPane scrollResultados = new JScrollPane(TablaResultados);
        scrollResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));

        // Crear botón para analizar
        btnAnalizar = new JButton("Analizar Cadenas");
        btnAnalizar.addActionListener(e -> construirAutomataDesdeTablas());

        // Panel inferior para el JTextArea, la tabla de resultados y el botón
        JPanel panelInferior = new JPanel(new BorderLayout());

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(scrollCadenas, BorderLayout.CENTER);
        panelSuperior.add(btnAnalizar, BorderLayout.SOUTH);

        panelInferior.add(panelSuperior, BorderLayout.NORTH);
        panelInferior.add(scrollResultados, BorderLayout.CENTER);

        // Agregar panelInferior al PanelConfi principal
        this.add(panelInferior, BorderLayout.SOUTH);

        // Acción para limpiar tablas (puedes mantenerla si la necesitas)
        // BntOff.addActionListener(e -> limpiarTablas());
    }

    public JTable getSimboloTable() {
        return Simbolo;
    }

    public JTable getEstadoDeAceptacionTable() {
        return EstadoDeAceptacion;
    }

    public JTable getTransicionTable() {
        return Transicion;
    }

    public JTextArea getCadenasATestear() {
        return CadenasATestear;
    }

    public JTextField getEstadoInicialField() {
        return EstadoInicial;
    }

    private void construirAutomataDesdeTablas() {
        try {
            // === Leer símbolos ===
            Set<Character> simbolos = new HashSet<>();
            DefaultTableModel simbolosModel = (DefaultTableModel) Simbolo.getModel();
            for (int i = 0; i < simbolosModel.getRowCount(); i++) {
                Object valor = simbolosModel.getValueAt(i, 1);
                if (valor != null && !valor.toString().trim().isEmpty()) {
                    simbolos.add(valor.toString().charAt(0));
                }
            }

            // === Leer estados de aceptación ===
            Set<String> aceptacion = new HashSet<>();
            DefaultTableModel aceptacionModel = (DefaultTableModel) EstadoDeAceptacion.getModel();
            for (int i = 0; i < aceptacionModel.getRowCount(); i++) {
                Object valor = aceptacionModel.getValueAt(i, 1);
                if (valor != null && !valor.toString().trim().isEmpty()) {
                    aceptacion.add(valor.toString().trim());
                }
            }

            // === Leer estados y transiciones ===
            Set<String> estados = new HashSet<>();
            Map<String, Map<Character, String>> transiciones = new HashMap<>();
            DefaultTableModel transicionModel = (DefaultTableModel) Transicion.getModel();

            // First, collect all states from the transition table
            for (int i = 0; i < transicionModel.getRowCount(); i++) {
                Object estadoObj = transicionModel.getValueAt(i, 0);
                if (estadoObj != null && !estadoObj.toString().trim().isEmpty()) {
                    String estado = estadoObj.toString().trim();
                    estados.add(estado);
                }
            }

            // Then, build transitions
            for (int i = 0; i < transicionModel.getRowCount(); i++) {
                Object estadoObj = transicionModel.getValueAt(i, 0);
                if (estadoObj == null || estadoObj.toString().trim().isEmpty()) {
                    continue;
                }

                String estado = estadoObj.toString().trim();
                Map<Character, String> mapa = new HashMap<>();

                for (int j = 1; j < transicionModel.getColumnCount(); j++) {
                    Object destino = transicionModel.getValueAt(i, j);
                    if (destino != null && !destino.toString().trim().isEmpty()) {
                        // Get symbol from column name
                        String columnName = transicionModel.getColumnName(j);
                        if (columnName.length() > 0) {
                            char simbolo = columnName.charAt(0);
                            mapa.put(simbolo, destino.toString().trim());
                            estados.add(destino.toString().trim()); // Add destination state
                        }
                    }
                }
                transiciones.put(estado, mapa);
            }

            // Get initial state from the text field
            String estadoInicial = EstadoInicial.getText().trim();
            if (estadoInicial.isEmpty()) {
                // Si está vacío, intentar obtener del primer estado de aceptación
                if (aceptacionModel.getRowCount() > 0) {
                    Object initState = aceptacionModel.getValueAt(0, 1);
                    if (initState != null && !initState.toString().trim().isEmpty()) {
                        estadoInicial = initState.toString().trim();
                        EstadoInicial.setText(estadoInicial); // Actualizar el campo
                    }
                }
            }

            // Si aún está vacío, usar Q0 por defecto
            if (estadoInicial.isEmpty()) {
                estadoInicial = "Q0";
                EstadoInicial.setText(estadoInicial);
            }

            // === Validate automata ===
            if (estados.isEmpty()) {
                throw new IllegalArgumentException("No se definieron estados");
            }
            if (simbolos.isEmpty()) {
                throw new IllegalArgumentException("No se definieron símbolos");
            }
            if (!estados.contains(estadoInicial)) {
                throw new IllegalArgumentException("Estado inicial no existe: " + estadoInicial);
            }

            // === Construir autómata ===
            automata = new Operacion(estados, simbolos, estadoInicial, aceptacion, transiciones);

            // === Analizar cadenas ===
            List<String> listaCadenas = new ArrayList<>();
            String textoCadenas = CadenasATestear.getText().trim();
            if (!textoCadenas.isEmpty()) {
                // Split by commas or newlines
                String[] lineas = textoCadenas.split("[,\\n\\r]+");
                for (String linea : lineas) {
                    String cadenaLimpia = linea.trim();
                    if (!cadenaLimpia.isEmpty()) {
                        listaCadenas.add(cadenaLimpia);
                    }
                }
            }

            if (!listaCadenas.isEmpty()) {
                Map<String, Boolean> resultados = automata.analizarCadenas(listaCadenas);
                mostrarResultados(resultados);
                JOptionPane.showMessageDialog(null, "Cadenas analizadas ✅");
            } else {
                JOptionPane.showMessageDialog(null, "Autómata generado, pero no hay cadenas para analizar");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error construyendo autómata: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void mostrarResultados(Map<String, Boolean> resultados) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Cadena", "Resultado"}, 0);
        for (Map.Entry<String, Boolean> entry : resultados.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue() ? "Aceptada" : "Rechazada"});
        }
        TablaResultados.setModel(model);
    }

    private void limpiarTablas() {
        ((DefaultTableModel) Simbolo.getModel()).setRowCount(0);
        ((DefaultTableModel) EstadoDeAceptacion.getModel()).setRowCount(0);
        ((DefaultTableModel) Transicion.getModel()).setRowCount(0);
        ((DefaultTableModel) TablaResultados.getModel()).setRowCount(0);
        CadenasATestear.setText("");
        EstadoInicial.setText("");
        JOptionPane.showMessageDialog(null, "Tablas y resultados limpiados 🗑️");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        EstadoDeAceptacion = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        Simbolo = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        Transicion = new javax.swing.JTable();
        EstadoInicial = new javax.swing.JTextField();

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        jPanel1.setPreferredSize(new java.awt.Dimension(325, 580));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setText("Estado Inicial:");

        EstadoDeAceptacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "No.", "Estado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(EstadoDeAceptacion);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel3.setText("Estados De Aceptacion:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Simbolos:");

        Simbolo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "No.", "Simbolo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(Simbolo);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel4.setText("Transiciones");

        Transicion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Estado", "0", "1"
            }
        ));
        jScrollPane4.setViewportView(Transicion);

        EstadoInicial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EstadoInicialActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(EstadoInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2)))
                    .addComponent(jLabel3))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(EstadoInicial, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(13, 13, 13)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void EstadoInicialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EstadoInicialActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_EstadoInicialActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable EstadoDeAceptacion;
    private javax.swing.JTextField EstadoInicial;
    private javax.swing.JTable Simbolo;
    private javax.swing.JTable Transicion;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable3;
    // End of variables declaration//GEN-END:variables
}
