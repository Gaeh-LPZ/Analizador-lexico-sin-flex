package com.gui;

import com.lexer.Lexer;
import com.lexer.Token;
import com.lexer.tipoToken;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AnalizadorLexicoPanel extends JPanel {

    private JTextField filePathField;
    private JTextArea textArea;

    public AnalizadorLexicoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel superior ---
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblSelect = new JLabel("Seleccione un programa fuente:");
        filePathField = new JTextField();
        filePathField.setEditable(false);

        JButton btnOpen   = new JButton("Abrir");
        JButton btnAnalyze = new JButton("Analizar");
        JButton btnClear  = new JButton("Limpiar");

        // Abrir archivo y mostrar en área de texto
        btnOpen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir"))); // <- aquí
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Archivos Java o Texto", "java", "txt"));

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
                loadFileContent(file);
            }
        });

        btnAnalyze.addActionListener(e -> {
        String code = textArea.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Ingrese un texto o abra un archivo para analizar.",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            // 3. Ejecutar el lexer directamente sobre el texto
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.scanTokens();

            // --- Tabla de tokens ---
            List<Object[]> listaTokens = new ArrayList<>();
            for (Token t : tokens) {
                if (t.tipo != tipoToken.IDENTIFICADOR && t.tipo != tipoToken.DESCONOCIDO) {
                    listaTokens.add(new Object[]{t.linea, t.lexema, t.tipo});
                }
            }

            // --- Tabla de símbolos ---
            List<Object[]> listaSimbolos = new ArrayList<>();
            Set<String> idsUnicos = new HashSet<>();
            int idCounter = 1;
            for (Token t : tokens) {
                if (t.tipo == tipoToken.IDENTIFICADOR && idsUnicos.add(t.lexema)) {
                    listaSimbolos.add(new Object[]{ idCounter++, t.lexema });
                }
            }

            // --- Tabla de errores ---
            List<Object[]> listaErrores = new ArrayList<>();
            for (Token t : tokens) {
                if (t.tipo == tipoToken.DESCONOCIDO) {
                    listaErrores.add(new Object[]{ t.linea, "Token desconocido: " + t.lexema });
                }

               if (t.tipo == tipoToken.ERROR_DE_CADENA) {
                    listaErrores.add(new Object[]{ t.linea, "error de cadena: " + t.lexema });
                }
    
            }

            // 4. Mostrar resultados
            ResultadosLexicosFrame resultados = new ResultadosLexicosFrame(
                listaTokens.toArray(new Object[0][]),
                listaSimbolos.toArray(new Object[0][]),
                listaErrores.toArray(new Object[0][])
            );
            resultados.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al analizar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        });

        // Limpiar
        btnClear.addActionListener(e -> {
            filePathField.setText("");
            textArea.setText("");
        });

        // Layout del topPanel
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(lblSelect, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        topPanel.add(filePathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        topPanel.add(btnOpen, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        topPanel.add(btnAnalyze, gbc);
        gbc.gridx = 2;
        topPanel.add(btnClear, gbc);

        add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setEditable(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    /** Carga el contenido de un archivo en el JTextArea */
    private void loadFileContent(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            textArea.setText("");
            String line;
            while ((line = reader.readLine()) != null) {
                textArea.append(line + "\n");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
