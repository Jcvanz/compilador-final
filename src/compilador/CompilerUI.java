package compilador;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CompilerUI extends JFrame implements ToolbarPanel.Commands {

    // Componentes principais
    private EditorPanel editorPanel;
    private MessagesPanel messagesPanel;
    private StatusBarPanel statusBar;

    // Estado
    private File currentFile = null;

    public CompilerUI() {
        super("Meu Compilador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 800);
        setResizable(false);
        setLocationRelativeTo(null);

        Container root = getContentPane();
        root.setLayout(new BorderLayout());

        // Toolbar
        ToolbarPanel toolBar = new ToolbarPanel(this);
        toolBar.setFloatable(false);
        toolBar.setPreferredSize(new Dimension(0, 70));
        root.add(toolBar, BorderLayout.NORTH);

        // Editor + Mensagens
        editorPanel = new EditorPanel();
        messagesPanel = new MessagesPanel();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                editorPanel.getScrollPane(),
                messagesPanel.getScrollPane());
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setDividerSize(10);
        split.setBorder(null);

        // Divisor
        split.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                BasicSplitPaneDivider div = new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setColor(new Color(228, 228, 228));
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(new Color(0x888888));
                        g2.drawLine(0, 0, getWidth(), 0);
                        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                        int mid = getHeight() / 2;
                        g2.setColor(new Color(0x777777));
                        for (int dx = getWidth() / 2 - 20; dx <= getWidth() / 2 + 20; dx += 8) {
                            g2.fillRect(dx, mid - 1, 3, 3);
                        }
                    }
                };
                div.setBorder(BorderFactory.createEmptyBorder());
                div.setBackground(new Color(0xBBBBBB));
                return div;
            }
        });

        editorPanel.getScrollPane().setMinimumSize(new Dimension(200, 150));
        messagesPanel.getScrollPane().setMinimumSize(new Dimension(200, 100));
        split.setResizeWeight(0.75);

        SwingUtilities.invokeLater(() -> split.setDividerLocation((int) (split.getHeight() * 0.75)));

        root.add(split, BorderLayout.CENTER);

        // Status bar
        statusBar = new StatusBarPanel();
        statusBar.setPreferredSize(new Dimension(0, 40));
        root.add(statusBar, BorderLayout.SOUTH);

        updateStatusPath();
        // Atalhos
        toolBar.installShortcuts(getRootPane());
    }

    // Atualização do status
    private void updateStatusPath() {
        statusBar.update(currentFile);
        setTitle(currentFile == null ? "Meu Compilador" : "Meu Compilador — " + currentFile.getName());
    }

    // Implementação das ações da Toolbar

    @Override
    public void onNew() {
        editorPanel.getTextArea().setText("");
        editorPanel.getTextArea().setCaretPosition(0);
        messagesPanel.getTextArea().setText("");
        currentFile = null;
        updateStatusPath();
    }

    @Override
    public void onOpen() {
        JFileChooser chooser = FileDialogs.makeTxtFileChooser(currentFile);
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!FileDialogs.isTxt(f)) {
                JOptionPane.showMessageDialog(this, "Selecione um arquivo .txt", "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String txt = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                editorPanel.getTextArea().setText(txt);
                editorPanel.getTextArea().setCaretPosition(0);
                messagesPanel.getTextArea().setText("");
                currentFile = f;
                updateStatusPath();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao abrir: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onSave() {
        if (currentFile == null) {
            onSaveAs();
        } else {
            try {
                Files.writeString(currentFile.toPath(), editorPanel.getTextArea().getText(), StandardCharsets.UTF_8);
                messagesPanel.getTextArea().setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveAs() {
        JFileChooser chooser = FileDialogs.makeTxtFileChooser(currentFile);
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = FileDialogs.ensureTxtExtension(chooser.getSelectedFile());
            try {
                Files.writeString(f.toPath(), editorPanel.getTextArea().getText(), StandardCharsets.UTF_8);
                messagesPanel.getTextArea().setText("");
                currentFile = f;
                updateStatusPath();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onCopy() {
        editorPanel.getTextArea().copy();
    }

    @Override
    public void onPaste() {
        editorPanel.getTextArea().paste();
    }

    @Override
    public void onCut() {
        editorPanel.getTextArea().cut();
    }

    @Override
    public void onCompile() {
        final JTextArea editorTA = editorPanel.getTextArea();
        messagesPanel.clear();

        String src = editorTA.getText();
        LexerService svc = new LexerService();
        LexerService.LexResult res = svc.analyze(src, editorTA);

        // Mostra a mensagem já pronta do serviço (preferível)
        if (res != null && res.message != null && !res.message.isEmpty()) {
            messagesPanel.showText(res.message);
            return;
        }

        // Fallback: montar mensagem a partir de campos estruturados
        StringBuilder sb = new StringBuilder();
        if (res != null && res.ok) {
            sb.append("programa compilado com sucesso\n");
        } else {
            sb.append("programa apresenta erro\n");
            if (res != null && res.error != null) {
                LexerService.LexErrorInfo e = res.error;
                switch (e.type) {
                    case INVALID_SYMBOL:
                        sb.append("linha ").append(e.line).append(": ")
                          .append(e.offending == null ? "símbolo inválido" : e.offending + " símbolo inválido")
                          .append('\n');
                        break;
                    case INVALID_IDENTIFIER:
                        sb.append("linha ").append(e.line).append(": identificador inválido\n");
                        break;
                    case INVALID_STRING:
                        sb.append("linha ").append(e.line).append(": constante_string inválida\n");
                        break;
                    case INVALID_COMMENT:
                        sb.append("linha ").append(e.line).append(": comentário inválido ou não finalizado\n");
                        break;
                    default:
                        sb.append("linha ").append(e.line).append(": ")
                          .append(e.rawMessage == null ? "erro léxico/sintático" : e.rawMessage)
                          .append('\n');
                        break;
                }
            } else {
                sb.append("erro desconhecido\n");
            }
        }

        messagesPanel.showText(sb.toString());
    }

    @Override
    public void onTeam() {
        JTextArea m = messagesPanel.getTextArea();
        m.setText("");
        m.append("Equipe de desenvolvimento:\n");
        m.append(" - Julio Cesar Vanz\n");
        m.append(" - Pedro Valle");
        m.setCaretPosition(m.getDocument().getLength());
    }

    // MAIN
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("ScrollBar.width", 16);
            UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32));
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new CompilerUI().setVisible(true));
    }
}
