package com.coleccionCanonica;

import java.util.*;

public class coleccionCanonica {
    private Map<String, List<List<String>>> gramatica;
    private String simboloInicial;
    private Set<String> noTerminales;
    
    public coleccionCanonica(Map<String, List<List<String>>> gramatica, String simboloInicial,Set<String> noTerminales) {
        this.gramatica = gramatica;
        this.simboloInicial = simboloInicial;
        this.noTerminales = noTerminales;
    }
    
    // Algoritmo de cerradura
    public Set<Item> cerradura(Set<Item> items) {
        Set<Item> cerradura = new HashSet<>(items);
        boolean cambio = true;
        
        while (cambio) {
            cambio = false;
            Set<Item> nuevosItems = new HashSet<>();
            
            for (Item item : cerradura) {
                String simbolo = item.getSiguienteSimbolo();
                
                // Si el símbolo después del punto es un no terminal
                if (simbolo != null && noTerminales.contains(simbolo)) {
                    // Agregar todos los items que empiezan con ese no terminal
                    if (gramatica.containsKey(simbolo)) {
                        for (List<String> produccion : gramatica.get(simbolo)) {
                            Item nuevoItem = new Item(produccion, simbolo, 0);
                            if (!cerradura.contains(nuevoItem)) {
                                nuevosItems.add(nuevoItem);
                                cambio = true;
                            }
                        }
                    }
                }
            }
            
            cerradura.addAll(nuevosItems);
        }
        
        return cerradura;
    }
    
    // Algoritmo ir_a
    public Set<Item> irA(Set<Item> items, String simbolo) {
        Set<Item> j = new HashSet<>();
        
        for (Item item : items) {
            String sigSimbolo = item.getSiguienteSimbolo();
            
            // Si el símbolo después del punto coincide con el símbolo de entrada
            if (simbolo.equals(sigSimbolo)) {
                j.add(item.avanzarPunto());
            }
        }
        
        // Retornar la cerradura del conjunto resultante
        if (!j.isEmpty()) {
            return cerradura(j);
        }
        return new HashSet<>();
    }
    
    // Algoritmo de coleccionCanonica
    public List<Set<Item>> calcularColeccion() {
        // Crear el item inicial S' -> •S
        List<String> produccionInicial = new ArrayList<>();
        produccionInicial.add(simboloInicial);
        Item itemInicial = new Item(produccionInicial, "S'", 0);
        
        // C = {CERRADURA({S' -> •S})}
        List<Set<Item>> c = new ArrayList<>();
        Set<Item> estadoInicial = cerradura(Set.of(itemInicial));
        c.add(estadoInicial);
        
        // Obtener todos los símbolos de la gramática
        Set<String> todosSimbolos = new HashSet<>(noTerminales);
        for (List<List<String>> producciones : gramatica.values()) {
            for (List<String> prod : producciones) {
                todosSimbolos.addAll(prod);
            }
        }
        
        boolean cambio = true;
        while (cambio) {
            cambio = false;
            List<Set<Item>> nuevosEstados = new ArrayList<>();
            
            // Para cada conjunto de items en C
            for (Set<Item> estado : c) {
                // Para cada símbolo X
                for (String simbolo : todosSimbolos) {
                    // Calcular IR_A(estado, X)
                    Set<Item> nuevoEstado = irA(estado, simbolo);
                    
                    // Si el nuevo estado no está vacío y no está en C
                    if (!nuevoEstado.isEmpty() && 
                        !c.contains(nuevoEstado) && 
                        !nuevosEstados.contains(nuevoEstado)) {
                        nuevosEstados.add(nuevoEstado);
                        cambio = true;
                    }
                }
            }
            
            c.addAll(nuevosEstados);
        }
        
        return c;
    }
}