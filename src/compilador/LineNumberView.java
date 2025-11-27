package compilador;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

class LineNumberView extends JComponent
        implements CaretListener, DocumentListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    private static final int MARGIN = 8;
    private final JTextArea textArea;
    private int lastDigits = 2;

    // mínimo de linhas exibidas pela régua (mesmo sem texto)
    private final int minLines;

    private JViewport viewport;
    private final ChangeListener viewportListener = e -> repaint();

    LineNumberView(JTextArea textArea) {
        this(textArea, 40); // mínimo padrão: 40 linhas
    }

    LineNumberView(JTextArea textArea, int minLines) {
        this.textArea = textArea;
        this.minLines = Math.max(1, minLines);

        // fonte 
        setFont(textArea.getFont().deriveFont(
            Math.max(10f, textArea.getFont().getSize2D() - 2f)
        ));
        setForeground(new Color(120, 120, 120));
        setBackground(new Color(245, 245, 245));
        setOpaque(true);

        textArea.getDocument().addDocumentListener(this);
        textArea.addCaretListener(this);
        textArea.addPropertyChangeListener(this);

        attachViewportListener();
        textArea.addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & (HierarchyEvent.PARENT_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED)) != 0) {
                    attachViewportListener();
                }
            }
        });

        updateWidth();
    }

    private void attachViewportListener() {
        JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, textArea);
        JViewport vp = (sp != null) ? sp.getViewport() : null;
        if (viewport == vp) return;
        if (viewport != null) viewport.removeChangeListener(viewportListener);
        viewport = vp;
        if (viewport != null) viewport.addChangeListener(viewportListener);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int lh = Math.max(fm.getHeight(), 12);

        // altura mínima equivalente a 'minLines' linhas
        int linesForHeight = Math.max(minLines, Math.max(1, textArea.getLineCount()));
        int height = linesForHeight * lh;

        int charW = fm.charWidth('0');
        return new Dimension(MARGIN * 2 + (lastDigits * Math.max(charW, 7)), height);
    }

    private void updateWidth() {
        int linesForDigits = Math.max(minLines, Math.max(1, textArea.getLineCount()));
        int digits = String.valueOf(linesForDigits).length();
        if (digits != lastDigits) {
            lastDigits = digits;
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // fundo + separador
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(200, 200, 200));
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

        Rectangle vis = textArea.getVisibleRect();
        FontMetrics fm = getFontMetrics(getFont());
        int lineHeight = Math.max(fm.getHeight(), 12);
        int ascent = fm.getAscent();

        int startLine = Math.max(0, vis.y / lineHeight);
        int visLines  = Math.max(1, vis.height / lineHeight + 1);

        // total de linhas consideradas
        int totalLines = Math.max(minLines, Math.max(1, textArea.getLineCount()));
        int endLine = Math.min(totalLines - 1, startLine + visLines - 1);

        g.setColor(getForeground());

        // desenha somente o range visível (alinhado ao editor)
        for (int line = startLine; line <= endLine; line++) {
            String s = String.valueOf(line + 1);
            int strW = fm.stringWidth(s);
            int x = getPreferredSize().width - strW - MARGIN - 1;

            int baseline = (line - startLine) * lineHeight + ascent;
            g.drawString(s, x, baseline);
        }
    }

    // listeners
    @Override public void caretUpdate(CaretEvent e) { repaint(); }
    @Override public void insertUpdate(DocumentEvent e) { updateWidth(); repaint(); }
    @Override public void removeUpdate(DocumentEvent e) { updateWidth(); repaint(); }
    @Override public void changedUpdate(DocumentEvent e) { updateWidth(); repaint(); }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        if ("font".equals(evt.getPropertyName())) {
            setFont(textArea.getFont().deriveFont(
                Math.max(10f, textArea.getFont().getSize2D() - 2f)
            ));
            updateWidth();
            repaint();
        } else if ("document".equals(evt.getPropertyName())) {
            Document oldDoc = (Document) evt.getOldValue();
            Document newDoc = (Document) evt.getNewValue();
            if (oldDoc != null) oldDoc.removeDocumentListener(this);
            if (newDoc != null) newDoc.addDocumentListener(this);
            updateWidth();
            repaint();
        }
    }
}
