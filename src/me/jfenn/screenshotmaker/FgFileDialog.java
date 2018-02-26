package me.jfenn.screenshotmaker;

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
        if (b) SwingUtilities.invokeLater(() -> frame.toFront());
    }
}
