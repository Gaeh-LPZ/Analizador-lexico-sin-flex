package com.Analizador;

import com.lexer.Lexer;
import com.lexer.Token;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Analizador {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== ANALIZADOR LÉXICO ===");
        System.out.print("Ingrese la ruta del archivo fuente (.txt): ");
        String filePath = scanner.nextLine();

        try {
            String sourceCode = readFile(filePath);
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.scanTokens();

            // Mostrar tira de tokens
            System.out.println("\n--- TIRA DE TOKENS ---");
            for (Token token : tokens) {
                System.out.println(token);
            }

            //  tabla de símbolos y errores si se implementa

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
