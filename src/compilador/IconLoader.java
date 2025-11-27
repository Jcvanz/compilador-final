package compilador;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

class IconLoader {
    static Icon load(String fileName, int size) {
        try {
            URL url = IconLoader.class.getResource("/img/" + fileName);
            if (url == null) {
                File f = new File("img", fileName);
                if (f.exists()) url = f.toURI().toURL();
            }
            if (url != null) {
                ImageIcon ic = new ImageIcon(url);
                Image scaled = ic.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {}
        return new FallbackGlyph(fileName);
    }

    // fallback
    private static class FallbackGlyph implements Icon {
        private final String label;
        FallbackGlyph(String file) {
            String base = file.replace(".png","").toUpperCase();
            this.label = base.length() > 2 ? base.substring(0,2) : base;
        }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getIconWidth(), h = getIconHeight();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(230,230,230));
            g2.fillOval(x, y, w, h);
            g2.setColor(new Color(120,120,120));
            g2.drawOval(x, y, w, h);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int gx = x + (w - fm.stringWidth(label)) / 2;
            int gy = y + (h + fm.getAscent() - fm.getDescent()) / 2 - 1;
            g2.drawString(label, gx, gy);
            g2.dispose();
        }
        @Override public int getIconWidth()  { return 22; }
        @Override public int getIconHeight() { return 22; }
    }
}
