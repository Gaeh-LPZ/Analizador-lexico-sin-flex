package com.gui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PrimerosSiguientesPanel extends JPanel {

    private static final String EPS = "ε";
    private JTextField filePathField;
    private JTextArea textArea;

    public PrimerosSiguientesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblSelect = new JLabel("Seleccione una gramática:");
        filePathField = new JTextField();
        filePathField.setEditable(false);

        JButton btnOpen    = new JButton("Abrir");
        JButton btnAnalyze = new JButton("Calcular");
        JButton btnClear   = new JButton("Limpiar");

        btnOpen.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir"))); 
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Archivos de Texto", "txt"));

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
                loadFileContent(file);
            }
        });


        btnAnalyze.addActionListener(e -> {
            String grammarText = textArea.getText().trim();
            if (grammarText.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Ingrese una gramática o abra un archivo.",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                Grammar g = Grammar.fromText(grammarText);
                Map<String, Set<String>> first  = computeFirst(g);
                Map<String, Set<String>> follow = computeFollow(g, first);

                // Preparar datos para las tablas
                Object[][] tablaPrimeros = toRows(first);
                Object[][] tablaSiguientes = toRows(follow);

                ResultadosPrimerosSiguientesFrame frame = new ResultadosPrimerosSiguientesFrame(
                        tablaPrimeros,
                        tablaSiguientes
                );
                frame.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al analizar la gramática: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        btnClear.addActionListener(e -> {
            filePathField.setText("");
            textArea.setText("");
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        topPanel.add(lblSelect, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        topPanel.add(filePathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        topPanel.add(btnOpen, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.0;
        topPanel.add(btnAnalyze, gbc);
        gbc.gridx = 2;
        topPanel.add(btnClear, gbc);

        add(topPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        textArea.setEditable(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    private void loadFileContent(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            textArea.setText(sb.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al leer el archivo: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Object[][] toRows(Map<String, Set<String>> map) {
        List<String> nts = new ArrayList<>(map.keySet());
        Collections.sort(nts);
        Object[][] rows = new Object[nts.size()][2];
        for (int i = 0; i < nts.size(); i++) {
            String nt = nts.get(i);
            String setStr = map.get(nt).stream()
                    .sorted()
                    .collect(Collectors.joining(", "));
            rows[i][0] = nt;
            rows[i][1] = "{" + setStr + "}";
        }
        return rows;
    }

    private static class Grammar {
        Map<String, List<List<String>>> prods = new LinkedHashMap<>();
        String start; 
        Set<String> nonTerminals = new LinkedHashSet<>();
        Set<String> terminals = new LinkedHashSet<>();

        static Grammar fromText(String text) {
            Grammar g = new Grammar();
            String[] lines = text.split("\\R+");
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

                String[] sides = line.split("->");
                if (sides.length != 2) {
                    throw new IllegalArgumentException("Línea inválida (se espera 'A -> ...'): " + line);
                }

                String lhs = sides[0].trim();
                if (lhs.isEmpty()) {
                    throw new IllegalArgumentException("LHS vacío en línea: " + line);
                }
                if (g.start == null) g.start = lhs;
                g.nonTerminals.add(lhs);

                String rhsAll = sides[1].trim();
                String[] alts = rhsAll.split("\\|");
                for (String alt : alts) {
                    String altTrim = alt.trim();
                    List<String> symbols = tokenize(altTrim);
                    if (symbols.isEmpty()) {
                        throw new IllegalArgumentException("Producción vacía en: " + line);
                    }
                    g.prods.computeIfAbsent(lhs, k -> new ArrayList<>()).add(symbols);
                }
            }

            Set<String> rhsSymbols = new LinkedHashSet<>();
            for (List<List<String>> alts : g.prods.values()) {
                for (List<String> prod : alts) {
                    rhsSymbols.addAll(prod);
                }
            }
            for (String s : rhsSymbols) {
                if (!g.nonTerminals.contains(s) && !isEpsilon(s)) {
                    g.terminals.add(s);
                }
            }
            return g;
        }

        static List<String> tokenize(String alt) {
            if (alt.contains(" ")) {
                return Arrays.stream(alt.split("\\s+"))
                        .map(Grammar::normalizeEps)
                        .collect(Collectors.toList());
            }
            return Collections.singletonList(normalizeEps(alt));
        }

        static boolean isEpsilon(String s) {
            String n = s.trim();
            return n.equals(EPS) || n.equalsIgnoreCase("EPS") || n.equalsIgnoreCase("epsilon");
        }

        static String normalizeEps(String s) {
            return isEpsilon(s) ? EPS : s;
        }
    }

    private static Map<String, Set<String>> computeFirst(Grammar g) {
        Map<String, Set<String>> first = new LinkedHashMap<>();

        for (String nt : g.nonTerminals) {
            first.put(nt, new LinkedHashSet<>());
        }

        boolean changed;
        do {
            changed = false;

            for (Map.Entry<String, List<List<String>>> e : g.prods.entrySet()) {
                String A = e.getKey();
                for (List<String> alpha : e.getValue()) {

                    if (alpha.size() == 1 && Grammar.isEpsilon(alpha.get(0))) {
                        if (first.get(A).add(EPS)) changed = true;
                        continue;
                    }

                    boolean allNullable = true;
                    for (String X : alpha) {
                        Set<String> toAdd;
                        if (g.terminals.contains(X)) {
                            toAdd = new LinkedHashSet<>(Collections.singleton(X));
                        } else if (Grammar.isEpsilon(X)) {
                            toAdd = new LinkedHashSet<>(Collections.singleton(EPS));
                        } else {
                            toAdd = new LinkedHashSet<>(first.getOrDefault(X, Collections.emptySet()));
                        }

                        boolean localChanged = false;
                        for (String s : toAdd) {
                            if (!s.equals(EPS)) {
                                if (first.get(A).add(s)) localChanged = true;
                            }
                        }
                        if (localChanged) changed = true;

                        if (!toAdd.contains(EPS)) {
                            allNullable = false;
                            break;
                        }
                    }
                    if (allNullable) {
                        if (first.get(A).add(EPS)) changed = true;
                    }
                }
            }

        } while (changed);

        return first;
    }

    private static Map<String, Set<String>> computeFollow(Grammar g, Map<String, Set<String>> first) {
        Map<String, Set<String>> follow = new LinkedHashMap<>();
        for (String nt : g.nonTerminals) {
            follow.put(nt, new LinkedHashSet<>());
        }
        follow.get(g.start).add("$");

        boolean changed;
        do {
            changed = false;

            for (Map.Entry<String, List<List<String>>> e : g.prods.entrySet()) {
                String A = e.getKey();
                for (List<String> alpha : e.getValue()) {
                    for (int i = 0; i < alpha.size(); i++) {
                        String B = alpha.get(i);
                        if (!g.nonTerminals.contains(B)) continue;

                        // beta = Xi+1 ... Xn
                        List<String> beta = (i + 1 < alpha.size()) ? alpha.subList(i + 1, alpha.size())
                                                                   : Collections.emptyList();

                        if (!beta.isEmpty()) {
                            // FIRST(beta) \ {ε} ⊆ FOLLOW(B)
                            Set<String> firstBeta = firstOfSequence(beta, g, first);
                            boolean localChanged = false;
                            for (String s : firstBeta) {
                                if (!s.equals(EPS)) {
                                    if (follow.get(B).add(s)) localChanged = true;
                                }
                            }
                            if (localChanged) changed = true;

                            // si FIRST(beta) contiene ε => FOLLOW(A) ⊆ FOLLOW(B)
                            if (firstBeta.contains(EPS)) {
                                if (follow.get(B).addAll(follow.get(A))) changed = true;
                            }
                        } else {
                            // B al final de la producción: FOLLOW(A) ⊆ FOLLOW(B)
                            if (follow.get(B).addAll(follow.get(A))) changed = true;
                        }
                    }
                }
            }

        } while (changed);

        return follow;
    }

    private static Set<String> firstOfSequence(List<String> seq, Grammar g, Map<String, Set<String>> first) {
        Set<String> result = new LinkedHashSet<>();
        boolean allNullable = true;

        for (String X : seq) {
            Set<String> fx = new LinkedHashSet<>();
            if (g.terminals.contains(X)) {
                fx.add(X);
            } else if (Grammar.isEpsilon(X)) {
                fx.add(EPS);
            } else {
                fx.addAll(first.getOrDefault(X, Collections.emptySet()));
            }

            // agregar FIRST(X) \ {ε}
            for (String s : fx) {
                if (!s.equals(EPS)) result.add(s);
            }

            if (!fx.contains(EPS)) {
                allNullable = false;
                break;
            }
        }
        if (allNullable) result.add(EPS);
        return result;
    }
}
