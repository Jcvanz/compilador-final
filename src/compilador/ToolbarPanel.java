package compilador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class ToolbarPanel extends JToolBar {

    interface Commands {
        void onNew();
        void onOpen();
        void onSave();
        void onCopy();
        void onPaste();
        void onCut();
        void onCompile();
        void onTeam();
    }

    private final Commands commands;

    ToolbarPanel(Commands commands) {
        this.commands = commands;
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 4)); 
        setOpaque(true);
        setBackground(new Color(228, 228, 228));
        setBorder(new EmptyBorder(0, 0, 6, 0));

        final int BAR_H = 70;
        final Dimension BTN = new Dimension(100, (int)Math.round(BAR_H * 0.90));

        add(make("novo",     "ctrl-n", "novo.png",     BTN, commands::onNew));
        add(make("abrir",    "ctrl-o", "abrir.png",    BTN, commands::onOpen));
        add(make("salvar",   "ctrl-s", "salvar.png",   BTN, commands::onSave));
        add(make("copiar",   "ctrl-c", "copiar.png",   BTN, commands::onCopy));
        add(make("colar",    "ctrl-v", "colar.png",    BTN, commands::onPaste));
        add(make("recortar", "ctrl-x", "recortar.png", BTN, commands::onCut));
        add(make("compilar", "F7",     "compilar.png", BTN, commands::onCompile));
        add(make("equipe",   "F1",     "equipe.png",   BTN, commands::onTeam));

        // bordas dos botões
        Component[] btns = getComponents();
        Color edge = new Color(150,150,150);
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] instanceof JButton b) {
                int right = (i == btns.length - 1) ? 1 : 0;
                b.setBorder(BorderFactory.createMatteBorder(0, 1, 1, right, edge));
            }
        }
    }

    private JButton make(String nome, String atalho, String icon, Dimension size, Runnable run) {
        Icon ic = IconLoader.load(icon, 18);
        String label = nome + " [" + atalho.toLowerCase() + "]";
        JButton b = new JButton(label, ic);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setMaximumSize(size);

        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setIconTextGap(5);

        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBackground(new Color(240,236,236));
        b.setForeground(new Color(30,30,30));
        b.setBorder(BorderFactory.createEmptyBorder());

        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(label);
        b.addActionListener(e -> run.run());
        return b;
    }

    // Atalhos dos botões
    void installShortcuts(JRootPane root) {
        bind(root, "control N", "new",     e -> commands.onNew());
        bind(root, "control O", "open",    e -> commands.onOpen());
        bind(root, "control S", "save",    e -> commands.onSave());
        bind(root, "control C", "copy",    e -> commands.onCopy());
        bind(root, "control V", "paste",   e -> commands.onPaste());
        bind(root, "control X", "cut",     e -> commands.onCut());
        bind(root, "F7",        "compile", e -> commands.onCompile());
        bind(root, "F1",        "team",    e -> commands.onTeam());
    }
    private void bind(JRootPane root, String stroke, String key, java.awt.event.ActionListener act) {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke), key);
        root.getActionMap().put(key, new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { act.actionPerformed(e); }});
    }
}
