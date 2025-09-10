package com.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String codigo;
    private int inicio = 0;
    private int actual = 0;
    private int linea = 1;
    private final List<Token> tokens = new ArrayList<>();
    public Lexer(String codigo){
        this.codigo = codigo;
    }

    public void identificador() {
        while (esAlfaNumerico(mirar()))
            avanzar();
        String texto = codigo.substring(inicio, actual);
    }
    public void numero(){
        while (esDigito(mirar()))
            avanzar();
        // Analizar si es un numero flotante
        if (mirar() == '.' && esDigito(mirarSiguiente())){
            avanzar(); // consumir '.'
            while (esDigito(mirar()))
                avanzar();
            añadirToken(tipoToken.FLOAT);
        } else {
            añadirToken(tipoToken.INT);
        }
    }

    public void literal() {
        while(mirar() != '"' && !estaAlFinal()){
            if(mirar() == '\n')
                linea++;
            avanzar();
        }
        if(estaAlFinal()){
            System.out.println("Error en la linea " + linea + ": Cadena sin cerrar");
            return;
        }
        avanzar();
        String valor = codigo.substring(inicio + 1, actual - 1);
        añadirToken(tipoToken.CADENA, valor);
    }
    private char avanzar(){
        return codigo.charAt(actual++);
    }

    private boolean esAlfaNumerico(char letra) {
        return esLetra(letra) || esDigito(letra);
    }
    private char mirar() {
        if (estaAlFinal())
            return '\0';
        return codigo.charAt(actual);
    }

    private boolean esLetra(char letra) {
        return (letra >= 'a'  && letra <= 'z') || (letra >= 'A' && letra <= 'Z') || letra == '_';
    }

    private boolean esDigito(char digito){
        return digito >= '0' && digito <= '9';
    }

    private boolean estaAlFinal() {
        return actual >= codigo.length();
    }

    private char mirarSiguiente() {
        if (actual + 1 >= codigo.length())
            return '\0';
        return codigo.charAt(actual + 1);
    }

    private void añadirToken(tipoToken tipo) {
        añadirToken(tipo, null);
    }

    private void añadirToken(tipoToken tipo, Object literal ){
        String texto = codigo.substring(inicio, actual);
        // Si el codigo actual no es nulo, se usa ese para cadenas
        String lexema = (literal == null)  ? texto : literal.toString();
        tokens.add(new Token(tipo, lexema, linea, inicio));
    }
}