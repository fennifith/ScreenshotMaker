package me.jfenn.screenshotmaker.dialogs;

import com.sun.istack.internal.Nullable;
import me.jfenn.screenshotmaker.ScreenshotMaker;
import me.jfenn.screenshotmaker.components.JScreenshotViewer;
import me.jfenn.screenshotmaker.data.FrameData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

public class FrameEditorDialog {

    private FrameData originalFrame;
    private FrameData tempFrame;
    private ActionListener listener;
    private JScreenshotViewer jScreenshotViewer;
    private JDialog dialog;

    private String name;
    private File file;
    private int frameSide;
    private int frameTop;
    private String ratio;

    public FrameEditorDialog(Component parent, @Nullable FrameData frame) {
        originalFrame = frame;
        name = frame != null ? frame.getName() : "New Frame";
        file = frame != null ? frame.getFile() : null;
        frameSide = frame != null ? frame.getSide() : 140;
        frameTop = frame != null ? frame.getTop() : 300;
        ratio = frame != null ? frame.getRatioString() : "16/9";

        tempFrame = new FrameData("", file, frameSide, frameTop, ratio);

        jScreenshotViewer = new JScreenshotViewer();
        jScreenshotViewer.setTitle("");
        jScreenshotViewer.setDescription("");
        jScreenshotViewer.setBackgroundColor(Color.LIGHT_GRAY);
        jScreenshotViewer.setTextColor(Color.DARK_GRAY);
        jScreenshotViewer.setFrame(tempFrame);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            if (file != null) {
                if (!name.isEmpty()) {
                    if (!ratio.endsWith(":") && !ratio.startsWith(":")) {
                        for (FrameData defaultFrame : FrameData.DEFAULTS) {
                            if (defaultFrame.getName().equals(name)) {
                                JOptionPane.showMessageDialog(dialog, "An item already exists with this name.", "Error", JOptionPane.ERROR_MESSAGE);
                                hide();
                                return;
                            }
                        }

                        if (listener != null)
                            listener.onEdit(originalFrame, new FrameData(name, file, frameSide, frameTop, ratio));
                    } else
                        JOptionPane.showMessageDialog(dialog, "The ratio must be in an \"X:Y\" format!", "Error", JOptionPane.ERROR_MESSAGE);
                } else
                    JOptionPane.showMessageDialog(dialog, "You must choose a name for the frame!", "Error", JOptionPane.ERROR_MESSAGE);
            } else
                JOptionPane.showMessageDialog(dialog, "You must pick an image to use as the frame!", "Error", JOptionPane.ERROR_MESSAGE);
            hide();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> hide());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            if (listener != null)
                listener.onEdit(originalFrame, null);
            hide();
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel jInputPanel = new JPanel();
        jInputPanel.setLayout(new GridLayout(0, 2, 10, 8));
        jInputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        jInputPanel.add(new JLabel("Name"));
        JTextField jNameTextField = new JTextField(name, 10);
        jNameTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                name = jNameTextField.getText();
                if (name.contains(",")) {
                    name = name.replaceAll(",", "");
                    jNameTextField.setText(name);
                }

                name = name.trim();
            }
        });
        jInputPanel.add(jNameTextField);

        jInputPanel.add(new JLabel());
        JButton jImportButton = new JButton("Import Frame");
        jImportButton.addActionListener(e -> {
            importFile();
            onValueChange();
        });
        jInputPanel.add(jImportButton);

        jInputPanel.add(new JLabel("Side Offset"));
        JSpinner jSideSpinner = new JSpinner(new SpinnerNumberModel(frameSide, 0, Integer.MAX_VALUE, 1));
        jSideSpinner.addChangeListener(e -> {
            frameSide = (int) jSideSpinner.getValue();
            onValueChange();
        });
        jInputPanel.add(jSideSpinner);

        jInputPanel.add(new JLabel("Top Offset"));
        JSpinner jTopSpinner = new JSpinner(new SpinnerNumberModel(frameTop, 0, Integer.MAX_VALUE, 1));
        jTopSpinner.addChangeListener(e -> {
            frameTop = (int) jTopSpinner.getValue();
            onValueChange();
        });
        jInputPanel.add(jTopSpinner);

        jInputPanel.add(new JLabel("Screenshot Ratio"));
        JTextField jRatioTextField = new JTextField(ratio, 10);
        jRatioTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                ratio = jRatioTextField.getText();
                String newRatio = ratio.replaceAll("[^\\d:]", "");
                if (!newRatio.equals(ratio)) {
                    ratio = newRatio;
                    jRatioTextField.setText(newRatio);
                }

                if (ratio.indexOf(":") > 0 && !ratio.endsWith(":")) {
                    onValueChange();
                }
            }
        });
        jInputPanel.add(jRatioTextField);

        panel.add(BorderLayout.WEST, jScreenshotViewer);
        panel.add(BorderLayout.EAST, jInputPanel);

        JOptionPane optionPane = new JOptionPane(panel);
        optionPane.setOptions(originalFrame != null ? new JButton[]{okButton, cancelButton, deleteButton} : new JButton[]{okButton, cancelButton});
        dialog = optionPane.createDialog(parent, frame == null ? "New Frame" : "Edit Frame");
        dialog.setMinimumSize(new Dimension(700, 500));
        dialog.pack();
    }

    private void onValueChange() {
        if (file != null && file.equals(tempFrame.getFile())) {
            tempFrame.setSide(frameSide);
            tempFrame.setTop(frameTop);
            tempFrame.setRatio(ratio);
        } else {
            tempFrame = new FrameData("", file, frameSide, frameTop, ratio);
        }

        jScreenshotViewer.setFrame(tempFrame);
    }

    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    public void show() {
        dialog.setVisible(true);
    }

    private void hide() {
        dialog.setVisible(false);
    }

    public interface ActionListener {
        void onEdit(FrameData originalFrame, FrameData newFrame);
    }

    private void importFile() {
        FgFileDialog dialog = new FgFileDialog(null, "Choose a Screenshot", FileDialog.LOAD);
        dialog.setFilenameFilter((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
        dialog.setFile(file != null ? file.getAbsolutePath() : ScreenshotMaker.PATH_IMPORT_FOLDER);
        dialog.setVisible(true);

        String fileString = dialog.getFile();
        if (fileString != null) {
            this.file = new File(dialog.getDirectory(), fileString);
        }
    }

}
