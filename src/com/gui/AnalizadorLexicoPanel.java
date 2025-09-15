package com.gui;

import com.lexer.Lexer;
import com.lexer.Token;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AnalizadorLexicoPanel extends JPanel {

    private JTextField filePathField;
    private JTextArea textArea;

    public AnalizadorLexicoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel superior con GridBag ---
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

        // --- Abrir archivo y mostrar contenido ---
        btnOpen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter =
                    new FileNameExtensionFilter("Archivos Java o Texto", "java", "txt");
            chooser.setFileFilter(filter);

            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
                loadFileContent(file); // mostrar texto en el área central
            }
        });

        // --- Analizar el texto que esté actualmente en el área ---
        btnAnalyze.addActionListener(e -> {
            String code = textArea.getText();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay texto para analizar.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // 1. Ejecutar el lexer directamente sobre el texto del área
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.scanTokens();

                // 2. Pasar tokens a matriz para JTable
                List<Object[]> listaTokens = new ArrayList<>();
                for (Token t : tokens) {
                    listaTokens.add(new Object[]{ t.linea, t.lexema, t.tipo });
                }

                // Si tu Lexer maneja símbolos y errores, reemplaza estos arreglos
                Object[][] simbolos = new Object[0][0];
                Object[][] errores  = new Object[0][0];

                // 3. Mostrar ventana de resultados
                ResultadosLexicosFrame resultados = new ResultadosLexicosFrame(
                        listaTokens.toArray(new Object[0][]),
                        simbolos,
                        errores
                );
                resultados.setVisible(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al analizar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // --- Limpiar campos ---
        btnClear.addActionListener(e -> {
            filePathField.setText("");
            textArea.setText("");
        });

        // --- Distribución de botones y campos en la parte superior ---
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

        // --- Área de texto central editable para mostrar/editar el archivo ---
        textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setEditable(true); // <---- AHORA ES EDITABLE
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    /**
     * Carga el contenido del archivo seleccionado en el JTextArea
     */
    private void loadFileContent(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            textArea.setText("");
            String line;
            while ((line = reader.readLine()) != null) {
                textArea.append(line + "\n");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al leer el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
