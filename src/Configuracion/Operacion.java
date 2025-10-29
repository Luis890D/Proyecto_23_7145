/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Configuracion;

import java.util.*;

/**
 *
 * @author luisd
 */
public class Operacion {

    private Set<String> estados;
    private Set<Character> simbolos;
    private String estadoInicial;
    private Set<String> estadosAceptacion;
    private Map<String, Map<Character, String>> transiciones;

    public Operacion(Set<String> estados, Set<Character> simbolos,
            String estadoInicial, Set<String> estadosAceptacion,
            Map<String, Map<Character, String>> transiciones) {
        this.estados = estados;
        this.simbolos = simbolos;
        this.estadoInicial = estadoInicial;
        this.estadosAceptacion = estadosAceptacion;
        this.transiciones = transiciones;
    }

    // Analizar una sola cadena
    public boolean analizarCadena(String cadena) {
        String estadoActual = estadoInicial;

        for (char simbolo : cadena.toCharArray()) {
            if (!simbolos.contains(simbolo)) {
                throw new IllegalArgumentException("Símbolo inválido en la cadena: " + simbolo);
            }

            Map<Character, String> transicionesEstado = transiciones.get(estadoActual);
            if (transicionesEstado == null) {
                throw new IllegalArgumentException("No hay transiciones definidas para el estado: " + estadoActual);
            }

            String siguienteEstado = transicionesEstado.get(simbolo);
            if (siguienteEstado == null) {
                throw new IllegalArgumentException("No hay transición definida para el símbolo '"
                        + simbolo + "' en el estado '" + estadoActual + "'");
            }

            estadoActual = siguienteEstado;
        }
        return estadosAceptacion.contains(estadoActual);
    }

    // Analizar varias cadenas
    public Map<String, Boolean> analizarCadenas(List<String> cadenas) {
        Map<String, Boolean> resultados = new LinkedHashMap<>();
        for (String cadena : cadenas) {
            try {
                resultados.put(cadena, analizarCadena(cadena));
            } catch (Exception e) {
                resultados.put(cadena + " [ERROR: " + e.getMessage() + "]", false);
            }
        }
        return resultados;
    }

    // Método para debug: mostrar la estructura del autómata
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Autómata Finito Determinista\n");
        sb.append("============================\n");
        sb.append("Estados: ").append(estados).append("\n");
        sb.append("Símbolos: ").append(simbolos).append("\n");
        sb.append("Estado inicial: ").append(estadoInicial).append("\n");
        sb.append("Estados de aceptación: ").append(estadosAceptacion).append("\n");
        sb.append("Transiciones:\n");

        for (String estado : transiciones.keySet()) {
            Map<Character, String> trans = transiciones.get(estado);
            sb.append("  ").append(estado).append(": ");
            for (Map.Entry<Character, String> entry : trans.entrySet()) {
                sb.append(entry.getKey()).append("->").append(entry.getValue()).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
