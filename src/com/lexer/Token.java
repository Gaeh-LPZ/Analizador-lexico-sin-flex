package com.lexer;

public class Token {
    public final tipoToken tipo;
    public final String lexema;
    public final int linea;
    public final int columna;

    public Token(tipoToken tipo, String lexema, int linea, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
    }

    @Override
    public String toString() {
        return "Token [tipo=" + tipo + ",lexema=" + lexema + ", linea=" + linea + ", columna=" + columna + "]";
    }
}