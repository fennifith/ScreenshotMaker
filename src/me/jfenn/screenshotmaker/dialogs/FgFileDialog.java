package me.jfenn.screenshotmaker.dialogs;

import javax.swing.*;
import java.awt.*;

public class FgFileDialog extends FileDialog {

    private Frame frame;

    public FgFileDialog(Frame parent, String title, int mode) {
        super(parent, title, mode);
        setModal(true);
        frame = parent;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b && frame != null) SwingUtilities.invokeLater(() -> frame.toFront());
    }
}
