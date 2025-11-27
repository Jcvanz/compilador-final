package compilador;

import javax.swing.*;
import java.awt.*;

public class MessagesPanel {
    private final JTextArea ta = new JTextArea();
    private final JScrollPane sp;

    public MessagesPanel() {
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        ta.setLineWrap(false);
        sp = new JScrollPane(ta,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setBorder(BorderFactory.createTitledBorder("Mensagens"));
    }

    public JTextArea getTextArea() { return ta; }     
    public JScrollPane getScrollPane() { return sp; }

    public void clear() {
        ta.setText("");
        ta.setCaretPosition(0);
    }
    public void showText(String s) {
        ta.setText(s);
        ta.setCaretPosition(ta.getDocument().getLength());
    }
}
