package compilador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

class StatusBarPanel extends JPanel {
    private final JLabel pathLabel = new JLabel();
    private final JLabel posLabel  = new JLabel("");

    StatusBarPanel() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(4, 8, 4, 8));
        add(pathLabel, BorderLayout.WEST);
        add(posLabel,  BorderLayout.EAST);
    }

    void update(File currentFile) {
        if (currentFile == null) {
            pathLabel.setText("—");
            return;
        }
        String pasta   = (currentFile.getParent() != null) ? currentFile.getParent() : "";
        String arquivo = currentFile.getName();
        pathLabel.setText(pasta + java.io.File.separator + arquivo);
    }
}
