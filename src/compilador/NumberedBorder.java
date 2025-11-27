package compilador;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;

import javax.swing.JViewport;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;

class NumberedBorder extends AbstractBorder {

    private static final long serialVersionUID = -5089118025935944759L;

    private int lineHeight = 16;     
    private final int charWidthGuess = 7; 
    private final Color myColor;

    NumberedBorder() {
        myColor = new Color(164, 164, 164);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (!(c instanceof JTextArea)) return;
        JTextArea textArea = (JTextArea) c;

        Font font = textArea.getFont();
        FontMetrics metrics = g.getFontMetrics(font);
        lineHeight = Math.max(metrics.getHeight(), 12);

        Color oldColor = g.getColor();
        g.setColor(myColor);

        int firstVisibleLine = 1;
        int visibleLines;

        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, textArea);
        if (viewport != null) {
            Point viewPos = viewport.getViewPosition();
            int topLineZeroBased = Math.max(viewPos.y, 0) / lineHeight;
            firstVisibleLine = topLineZeroBased + 1;

            visibleLines = (textArea.getHeight() / lineHeight) + 1; 
        } else {
            // fallback
            visibleLines = Math.max(height / lineHeight, 1);
        }

        int totalLines = Math.max(textArea.getLineCount(), firstVisibleLine + visibleLines - 1);

        int digits = String.valueOf(totalLines).length();
        int lineLeft = digits * (metrics.charWidth('0') > 0 ? metrics.charWidth('0') : charWidthGuess) + 10;

        for (int i = 0; i < visibleLines; i++) {
            int lineNumber = firstVisibleLine + i;
            String str = String.valueOf(lineNumber);

            int strWidth = metrics.stringWidth(str);
            int px = lineLeft - strWidth - 2;
            int py = (i * lineHeight) + metrics.getAscent() + 2;

            g.drawString(str, px, py);
        }

        g.drawLine(lineLeft, 0, lineLeft, height);

        g.setColor(oldColor);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        if (!(c instanceof JTextArea)) return new Insets(1, 20, 1, 1);
        JTextArea textArea = (JTextArea) c;

        FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
        int totalLines = Math.max(textArea.getLineCount(), 1);
        int digits = String.valueOf(totalLines).length();

        int charW = metrics.charWidth('0') > 0 ? metrics.charWidth('0') : charWidthGuess;
        int left = (digits * charW) + 13; 

        return new Insets(1, left, 1, 1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets i = getBorderInsets(c);
        insets.top = i.top;
        insets.left = i.left;
        insets.bottom = i.bottom;
        insets.right = i.right;
        return insets;
    }
}
