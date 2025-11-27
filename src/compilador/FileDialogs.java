package compilador;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public final class FileDialogs {
    private FileDialogs() {}

    public static JFileChooser makeTxtFileChooser(File currentFile) {
        JFileChooser c = new JFileChooser();
        c.setAcceptAllFileFilterUsed(false);
        c.setFileFilter(new FileNameExtensionFilter("Arquivos de texto (*.txt)", "txt"));
        if (currentFile != null && currentFile.getParentFile() != null) {
            c.setCurrentDirectory(currentFile.getParentFile());
        }
        return c;
    }

    public static boolean isTxt(File f) {
        return f.getName().toLowerCase().endsWith(".txt");
    }

    public static File ensureTxtExtension(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".txt") ? f : new File(f.getParentFile(), f.getName() + ".txt");
    }
}
