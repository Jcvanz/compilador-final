package compilador;

import javax.swing.*;
import java.awt.*;

class EditorPanel extends JPanel {

    private final JTextArea textArea;
    private final JScrollPane scroll;

    EditorPanel() {
        super(new BorderLayout());

        textArea = new JTextArea();
        textArea.setRows(40);
        textArea.setColumns(160);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setTabSize(4);
        textArea.setLineWrap(false);

        // garante altura para rolar mesmo vazio
        ensureMinHeight(textArea, 40);

        scroll = new JScrollPane(
            textArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        );
        scroll.setBorder(BorderFactory.createTitledBorder("Editor"));
        scroll.setRowHeaderView(new LineNumberView(textArea, 40));

        // Scrollbars
        scroll.getVerticalScrollBar().setUI(new ScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new ScrollBarUI());

        // Bordas separando viewport das barras
        scroll.setViewportBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(160,160,160))
        );

        add(scroll, BorderLayout.CENTER);
    }

    JTextArea getTextArea()   { return textArea; }
    JScrollPane getScrollPane(){ return scroll; }

    private static void ensureMinHeight(JTextArea area, int minLines) {
        FontMetrics fm = area.getFontMetrics(area.getFont());
        int lh = Math.max(fm.getHeight(), 12);
        int charW = fm.charWidth('M');
        int cols = Math.max(area.getColumns(), 120);
        Dimension pref = new Dimension(cols * charW + 50, minLines * lh + 10);
        area.setPreferredSize(pref);
        area.revalidate();
    }
}
