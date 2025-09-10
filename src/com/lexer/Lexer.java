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


    public List<Token> scanTokens() {
        while (!estaAlFinal()) {
            inicio = actual;
            scanToken();
        }
        // Anadir el token de fin de archivo
        tokens.add(new Token(tipoToken.EOF, "", linea, inicio));
        return tokens;
    }

    private void scanToken() {
        char c = avanzar();
        switch (c) {
            // símbolos de un solo carácter
            case '(': 
                addToken(tipoToken.PARENTESIS_IZQ);
                break;

            case ')': 
                addToken(tipoToken.PARENTESIS_DER);
                break;

            case '{': 
                addToken(tipoToken.LLAVE_IZQ);
                break;

            case '}': 
                addToken(tipoToken.LLAVE_DER);
                break;

            case ',': 
                addToken(tipoToken.COMA);
                break;

            case ';': 
                addToken(tipoToken.PUNTO_Y_COMA);
                break;

            case '+': 
                addToken(tipoToken.SUMA);
                break;

            case '-': 
                addToken(tipoToken.RESTA);
                break;

            case '*': 
                addToken(tipoToken.MULTIPLICACION);
                break;

            // operadores que podrían ser de uno o dos caracteres
            case '!': 
                addToken(match('=') ? tipoToken.DIFERENTE : tipoToken.DESCONOCIDO); // '!' solo no es válido
                break;

            case '=': 
                addToken(match('=') ? tipoToken.IGUAL : tipoToken.ASIGNACION);
                break;

            case '<': 
                addToken(match('=') ? tipoToken.MENOR_IGUAL : tipoToken.MENOR_QUE);
                break;

            case '>': 
                addToken(match('=') ? tipoToken.MAYOR_IGUAL : tipoToken.MAYOR_QUE);
                break;

            // la división es especial porque puede iniciar un comentario
            case '/':
                if (match('/')) {
                    // es un comentario, entonces avanza hasta el final de la línea
                    while (mirar() != '\n' && !estaAlFinal()) avanzar();
                } else {
                    addToken(tipoToken.DIVISION);
                }
                break;

            // ignorar espacios en blanco
            case ' ':
            case '\r':
            case '\t':
                break;

            // nueva línea
            case '\n':
                linea++;
                break;

            // literales de cadena
            case '"':
                literal();
                break;

            default:
                if (esDigito(c)) {
                    numero();
                } else if (esLetra(c)) {
                    identificador();
                } else {
                    // carácter no reconocido
                    addToken(tipoToken.DESCONOCIDO);
                }
                break;
        }
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


    private boolean match(char expected) {
        if (estaAlFinal() || codigo.charAt(actual) != expected) {
            return false;
        }
        actual++;
        return true;
    }
}