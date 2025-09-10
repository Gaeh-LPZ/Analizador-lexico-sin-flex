public class Analizador {
    public static void main(String[] args) {
        
    }


    public Lexer(String source){
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // Anadir el token de fin de archivo
        tokens.add(new Token(TokenType.EOF, "", line, start));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // símbolos de un solo carácter
            case '(': 
                addToken(TokenType.PARENTESIS_IZQ);
                break;

            case ')': 
                addToken(TokenType.PARENTESIS_DER);
                break;

            case '{': 
                addToken(TokenType.LLAVE_IZQ);
                break;

            case '}': 
                addToken(TokenType.LLAVE_DER);
                break;

            case ',': 
                addToken(TokenType.COMA);
                break;

            case ';': 
                addToken(TokenType.PUNTO_Y_COMA);
                break;

            case '+': 
                addToken(TokenType.SUMA);
                break;

            case '-': 
                addToken(TokenType.RESTA);
                break;

            case '*': 
                addToken(TokenType.MULTIPLICACION);
                break;

            // operadores que podrían ser de uno o dos caracteres
            case '!': 
                addToken(match('=') ? TokenType.DIFERENTE : TokenType.DESCONOCIDO); // '!' solo no es válido
                break;

            case '=': 
                addToken(match('=') ? TokenType.IGUAL : TokenType.ASIGNACION);
                break;

            case '<': 
                addToken(match('=') ? TokenType.MENOR_IGUAL : TokenType.MENOR_QUE);
                break;

            case '>': 
                addToken(match('=') ? TokenType.MAYOR_IGUAL : TokenType.MAYOR_QUE);
                break;

            // la división es especial porque puede iniciar un comentario
            case '/':
                if (match('/')) {
                    // es un comentario, entonces avanza hasta el final de la línea
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.DIVISION);
                }
                break;

            // ignorar espacios en blanco
            case ' ':
            case '\r':
            case '\t':
                break;

            // nueva línea
            case '\n':
                line++;
                break;

            // literales de cadena
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // carácter no reconocido
                    addToken(TokenType.DESCONOCIDO);
                }
                break;
        }
    }
}
