package com.coleccionCanonica;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Ejemplo de gramática:
        // E -> E + T | T
        // T -> T * F | F
        // F -> ( E ) | id
        
        Map<String, List<List<String>>> gramatica = new HashMap<>();
        Set<String> noTerminales = new HashSet<>();
        
        // Definir no terminales
        noTerminales.add("E");
        noTerminales.add("T");
        noTerminales.add("F");
        
        // Producción E -> E + T
        List<String> prod1 = Arrays.asList("E", "+", "T");
        // Producción E -> T
        List<String> prod2 = Arrays.asList("T");
        gramatica.put("E", Arrays.asList(prod1, prod2));
        
        // Producción T -> T * F
        List<String> prod3 = Arrays.asList("T", "*", "F");
        // Producción T -> F
        List<String> prod4 = Arrays.asList("F");
        gramatica.put("T", Arrays.asList(prod3, prod4));
        
        // Producción F -> ( E )
        List<String> prod5 = Arrays.asList("(", "E", ")");
        // Producción F -> id
        List<String> prod6 = Arrays.asList("id");
        gramatica.put("F", Arrays.asList(prod5, prod6));
        
        // Crear instancia del algoritmo
        coleccionCanonica cc = new coleccionCanonica(gramatica, "E", noTerminales);
        
        System.out.println("========================================");
        System.out.println("ALGORITMO DE COLECCION CANONICA");
        System.out.println("========================================\n");
        
        // Mostrar la gramática
        System.out.println("GRAMATICA:");
        System.out.println("---------");
        for (Map.Entry<String, List<List<String>>> entry : gramatica.entrySet()) {
            String nt = entry.getKey();
            for (List<String> prod : entry.getValue()) {
                System.out.println(nt + " -> " + String.join(" ", prod));
            }
        }
        System.out.println("\nSimbolo inicial: " + "E");
        System.out.println();
        
        // Calcular la colección canónica
        List<Set<Item>> coleccion = cc.calcularColeccion();
        
        // Mostrar los estados
        System.out.println("COLECCION CANONICA DE ITEMS LR(0):");
        System.out.println("===================================\n");
        
        for (int i = 0; i < coleccion.size(); i++) {
            System.out.println("I" + i + ":");
            for (Item item : coleccion.get(i)) {
                System.out.println("    " + item);
            }
            System.out.println();
        }
        
        System.out.println("Total de estados: " + coleccion.size());
        
        // Mostrar transiciones CON cerraduras
        System.out.println("\n========================================");
        System.out.println("TRANSICIONES CON CERRADURAS:");
        System.out.println("========================================\n");
        
        Set<String> todosSimbolos = new HashSet<>(noTerminales);
        todosSimbolos.addAll(Arrays.asList("+", "*", "(", ")", "id"));
        
        for (int i = 0; i < coleccion.size(); i++) {
            Set<Item> estado = coleccion.get(i);
            for (String simbolo : todosSimbolos) {
                // Obtener items antes de la cerradura (solo avanzar el punto)
                Set<Item> itemsAntesDecerradura = new HashSet<>();
                for (Item item : estado) {
                    String sigSimbolo = item.getSiguienteSimbolo();
                    if (simbolo.equals(sigSimbolo)) {
                        itemsAntesDecerradura.add(item.avanzarPunto());
                    }
                }
                
                if (!itemsAntesDecerradura.isEmpty()) {
                    // Calcular la cerradura completa
                    Set<Item> destino = cc.cerradura(itemsAntesDecerradura);
                    
                    if (!destino.isEmpty()) {
                        int indiceDestino = coleccion.indexOf(destino);
                        if (indiceDestino != -1) {
                            // Formato: ir_a(I0, simbolo) = cerradura({items})
                            System.out.print("ir_a(I" + i + ", " + simbolo + ") = cerradura({");
                            
                            // Mostrar items antes de la cerradura
                            List<Item> listaItems = new ArrayList<>(itemsAntesDecerradura);
                            for (int j = 0; j < listaItems.size(); j++) {
                                System.out.print("[" + listaItems.get(j) + "]");
                                if (j < listaItems.size() - 1) {
                                    System.out.print(", ");
                                }
                            }
                            
                            System.out.println("})");
                            
                            // Mostrar el resultado de la cerradura
                            System.out.println("  = {");
                            for (Item item : destino) {
                                System.out.println("      " + item);
                            }
                            System.out.println("    }");
                            System.out.println("  = I" + indiceDestino + "\n");
                        }
                    }
                }
            }
        }
    }
}