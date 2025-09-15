package com.gui;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
//import gui.AnalizadorLexicoPanel;


public class CompiladorMain extends JFrame {
    private DataProvider dataProvider;
    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    public CompiladorMain(DataProvider provider) {
        super("Compilador");
        this.dataProvider = provider;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 640);
        setLocationRelativeTo(null);

        // Barra de menús reutilizable
        setJMenuBar(new ReusableMenuBar(dataProvider, new MenuActionHandler()));

        // Área central con pestañas
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Inicio", createBannerPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Barra de estado
        statusLabel = new JLabel("Listo");
        statusLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createBannerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridy = 0;

        CodeSnippetPanel codePanel = new CodeSnippetPanel(
                "int main() {\n    printf(\"¡Hola compilador!\\n\");\n    return 0;\n}"
        );
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(codePanel, gbc);

        ImagePanel imagePanel = new ImagePanel(dataProvider.getBannerImagePath());
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(imagePanel, gbc);

        return panel;
    }

    private void openAnalyzerTab(String title, JComponent content) {
        JScrollPane scroll = new JScrollPane(content);

        tabbedPane.addTab(title, scroll);
        int index = tabbedPane.indexOfComponent(scroll);

        // Cabecera personalizada para la pestaña
        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        JLabel tabLabel = new JLabel(title + "  ");
        JButton closeBtn = new JButton("✕");
        closeBtn.setMargin(new Insets(0, 4, 0, 4));
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.setFocusable(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efecto hover en la X
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeBtn.setOpaque(true);
                closeBtn.setBackground(new Color(220, 70, 70)); // rojo suave
                closeBtn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeBtn.setOpaque(false);
                closeBtn.setBackground(null);
                closeBtn.setForeground(Color.BLACK);
            }
        });

        closeBtn.addActionListener(e -> tabbedPane.remove(scroll));

        tabHeader.add(tabLabel);
        tabHeader.add(closeBtn);

        tabbedPane.setTabComponentAt(index, tabHeader);
        tabbedPane.setSelectedComponent(scroll);

        statusLabel.setText("Abierta: " + title);
    }

    private class MenuActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            switch (cmd) {
                case "exit":
                    dispose();
                    break;
                case "analizador_lexico":
                    // 👉 Aquí abrimos tu panel personalizado
                    openAnalyzerTab("Analizador Léxico", new AnalizadorLexicoPanel());
                    break;
                default:
                    String content = dataProvider.getAlgorithmDescription(cmd);
                    if (content == null) content = "(sin contenido)";
                    JTextArea area = new JTextArea(content);
                    area.setEditable(false);
                    area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                    openAnalyzerTab(cmd, area);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Cambia entre FlatLightLaf (claro) y FlatDarkLaf (oscuro)
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            UIManager.put("defaultFont", new Font("JetBrains Mono", Font.PLAIN, 13));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            DataProvider provider = new DefaultDataProvider();
            CompiladorMain frame = new CompiladorMain(provider);
            frame.setVisible(true);
        });
    }
}

class ReusableMenuBar extends JMenuBar {
    public ReusableMenuBar(DataProvider provider, ActionListener handler) {
        JMenu archivo = new JMenu("Archivo");
        JMenuItem exit = new JMenuItem("Salir");
        exit.setActionCommand("exit");
        exit.addActionListener(handler);
        archivo.add(exit);
        add(archivo);

        JMenu lex = new JMenu("Analizador léxico");
        JMenuItem thompson = new JMenuItem("Algoritmo de Thompson AFN");
        thompson.setActionCommand("thompson_afn");
        thompson.addActionListener(handler);
        lex.add(thompson);

        JMenuItem conjuntos = new JMenuItem("Algoritmo de Construcción de Conjuntos");
        conjuntos.setActionCommand("construccion_conjuntos");
        conjuntos.addActionListener(handler);
        lex.add(conjuntos);

        JMenuItem analizadorLex = new JMenuItem("Analizador léxico");
        analizadorLex.setActionCommand("analizador_lexico");
        analizadorLex.addActionListener(handler);
        lex.add(analizadorLex);
        add(lex);

        JMenu sint = new JMenu("Analizador sintáctico");
        JMenuItem parser = new JMenuItem("Analizador sintáctico (placeholder)");
        parser.setActionCommand("analizador_sintactico");
        parser.addActionListener(handler);
        sint.add(parser);
        add(sint);

        JMenu sem = new JMenu("Analizador semántico");
        JMenuItem semItem = new JMenuItem("Analizador semántico (placeholder)");
        semItem.setActionCommand("analizador_semantico");
        semItem.addActionListener(handler);
        sem.add(semItem);
        add(sem);
    }
}

class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel(String imagePath) {
        try {
            if (imagePath == null) throw new IOException("ruta de imagen nula");
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            image = null;
            System.err.println("No se pudo cargar la imagen: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int pw = getWidth();
            int ph = getHeight();
            double imgRatio = (double) image.getWidth() / image.getHeight();
            double panelRatio = (double) pw / ph;

            int drawW, drawH;
            if (panelRatio > imgRatio) {
                drawH = ph;
                drawW = (int) (ph * imgRatio);
            } else {
                drawW = pw;
                drawH = (int) (pw / imgRatio);
            }
            int x = (pw - drawW) / 2;
            int y = (ph - drawH) / 2;
            g.drawImage(image, x, y, drawW, drawH, this);
        } else {
            g.setColor(Color.GRAY);
            g.setFont(g.getFont().deriveFont(Font.ITALIC, 14f));
            drawCenteredString(g, "Imagen no disponible. Coloca resources/compilador_banner.png", getWidth(), getHeight());
        }
    }

    private void drawCenteredString(Graphics g, String text, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, x, y);
    }
}

class CodeSnippetPanel extends JPanel {
    public CodeSnippetPanel(String code) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(12, 12, 12, 12)));
        setBackground(Color.WHITE);

        JTextArea area = new JTextArea(code);
        area.setEditable(false);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        area.setOpaque(false);
        add(area, BorderLayout.CENTER);

        setPreferredSize(new Dimension(420, 220));
    }
}

interface DataProvider {
    String getBannerImagePath();
    String getAlgorithmDescription(String key);
}

class DefaultDataProvider implements DataProvider {
    @Override
    public String getBannerImagePath() {
        return "resources/compilador_banner.png";
    }

    @Override
    public String getAlgorithmDescription(String key) {
        switch (key) {
            case "thompson_afn":
                return "Algoritmo de Thompson (AFN):\n\nConstruye un AFN a partir de una ER.";
            case "construccion_conjuntos":
                return "Algoritmo de Construcción de Conjuntos:\n\nConvierte AFN a AFD usando e-closure y move.";
            case "analizador_lexico":
                return "Analizador léxico (placeholder):\n\nEjecuta análisis léxico sobre texto de entrada.";
            case "analizador_sintactico":
                return "Analizador sintáctico (placeholder):\n\nImplementaremos parser y árbol sintáctico.";
            case "analizador_semantico":
                return "Analizador semántico (placeholder):\n\nChequeos semánticos y reporte de errores.";
            default:
                return "Comando: " + key + "\n\n(No hay contenido preparado para este comando.)";
        }
    }
}
