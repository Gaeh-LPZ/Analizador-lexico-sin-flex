package com.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Set;
import com.coleccionCanonica.ItemLR0;
import com.coleccionCanonica.Grammar;
import com.coleccionCanonica.CanonicalLR0;    

public class ColeccionCanonicaPanel extends JPanel {

    private final JTextField txtPath = new JTextField();
    private final JButton btnOpen = new JButton("Abrir");
    private final JButton btnAnalyze = new JButton("Analizar");
    private final JButton btnClear = new JButton("Limpiar");

    private final JTextArea txtOut = new JTextArea();
    private final JTextArea txtGrammar = new JTextArea();

    private File selectedFile = null;

    public ColeccionCanonicaPanel() {
        setLayout(new BorderLayout(8,8));

        // Top bar
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.gridx = 0; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        top.add(new JLabel("Seleccione una gramática aumentada:"), c);

        c.gridx = 1; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        txtPath.setEditable(false);
        top.add(txtPath, c);

        c.gridx = 2; c.gridy = 0;
        top.add(btnOpen, c);

        c.gridx = 3; c.gridy = 0;
        top.add(btnAnalyze, c);

        c.gridx = 4; c.gridy = 0;
        top.add(btnClear, c);

        add(top, BorderLayout.NORTH);

        // Split center
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        txtOut.setEditable(false);
        txtOut.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane spLeft = new JScrollPane(txtOut);
        spLeft.setBorder(new TitledBorder("Colección canónica"));

        txtGrammar.setEditable(false);
        txtGrammar.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane spRight = new JScrollPane(txtGrammar);
        spRight.setBorder(new TitledBorder("Gramática"));

        split.setLeftComponent(spLeft);
        split.setRightComponent(spRight);
        split.setResizeWeight(0.75);
        add(split, BorderLayout.CENTER);

        // Actions
        btnOpen.addActionListener(this::onOpen);
        btnAnalyze.addActionListener(this::onAnalyze);
        btnClear.addActionListener(e -> {
            txtPath.setText("");
            txtOut.setText("");
            txtGrammar.setText("");
            selectedFile = null;
        });
    }

    private void onOpen(ActionEvent e) {
        JFileChooser ch = new JFileChooser();

        // Abrir en directorio actual o último utilizado
        if (selectedFile != null && selectedFile.getParentFile() != null) {
            ch.setCurrentDirectory(selectedFile.getParentFile());
        } else {
            ch.setCurrentDirectory(new File(System.getProperty("user.dir")));
        }

        ch.setDialogTitle("Abrir gramática aumentada (incluye S'→S$)");
        int r = ch.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            selectedFile = ch.getSelectedFile();
            txtPath.setText(selectedFile.getAbsolutePath());
            try {
                txtGrammar.setText(Grammar.readWholeFile(selectedFile.getAbsolutePath()));
                txtOut.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo leer el archivo:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAnalyze(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Primero elige un archivo de gramática aumentada.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Grammar g = Grammar.parseAugmentedGrammar(selectedFile.getAbsolutePath());
            List<Set<ItemLR0>> C = CanonicalLR0.canonicalCollection(g);
            String report = CanonicalLR0.formatReport(g, C);
            txtOut.setText(report);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al analizar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Standalone runner (opcional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Cálculo de la colección canónica");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new ColeccionCanonicaPanel());
            f.setSize(1000, 650);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
