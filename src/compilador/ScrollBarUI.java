package compilador;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

class ScrollBarUI extends BasicScrollBarUI {
    private final Color TRACK = Color.WHITE;
    private final Color THUMB_FILL = Color.WHITE;
    private final Color THUMB_BORDER = new Color(140, 140, 140);

    @Override protected void configureScrollBarColors() {
        this.thumbColor = THUMB_FILL;
        this.trackColor = TRACK;
    }

    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        g.setColor(TRACK);
        g.fillRect(r.x, r.y, r.width, r.height);
    }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (!c.isEnabled() || r.width <= 0 || r.height <= 0) return;
        g.setColor(THUMB_FILL);
        g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
        g.setColor(THUMB_BORDER);
        g.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
    }

    @Override protected JButton createDecreaseButton(int orientation) {
        BasicArrowButton b = new BasicArrowButton(orientation, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(160,160,160)));
        return b;
    }
    @Override protected JButton createIncreaseButton(int orientation) {
        BasicArrowButton b = new BasicArrowButton(orientation, Color.WHITE, Color.WHITE, Color.BLACK, Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(160,160,160)));
        return b;
    }
}
