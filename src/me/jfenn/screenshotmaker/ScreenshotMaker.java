package me.jfenn.screenshotmaker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ScreenshotMaker {

    private Dimension screenSize;
    private File file;
    private boolean isChanged;

    private JFrame jFrame;
    private JScreenshotViewer jScreenshotViewer;
    private JTextField jTitleTextField;
    private JTextField jDescriptionTextField;
    private JSpinner jTextSizeSpinner;
    private JComboBox<String> jTextPositionComboBox;
    private JSpinner jOffsetSpinner;
    private JColorChooserButton jTextColorChooserButton;
    private JColorChooserButton jBackgroundColorChooserButton;
    private JComboBox jExportSizeComboBox;
    private JButton jImportButton;
    private JButton jExportButton;

    public ScreenshotMaker() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        init();
    }

    private void init() {
        jFrame = new JFrame("Screenshot Maker");
        jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame.setMinimumSize(new Dimension(Math.min((int) (screenSize.width / 1.5), 600), Math.min((int) (screenSize.height / 1.5), 350)));
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (file == null || isChanged) {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "You have unsaved changes to this template. Would you like to save it first?",
                            "Save File",
                            JOptionPane.YES_NO_CANCEL_OPTION);

                    if (choice == JOptionPane.YES_OPTION)
                        toFile(false);

                    if (choice != JOptionPane.CANCEL_OPTION)
                        jFrame.dispose();
                }
            }
        });

        // --------------------- Menu Elements ------------------ //

        JMenuBar jMenuBar = new JMenuBar();
        JMenu jFileMenu = new JMenu("File");

        JMenuItem jNewItem = new JMenuItem("New");
        jNewItem.addActionListener(e -> newProcess());
        jFileMenu.add(jNewItem);
        JMenuItem jOpenMenu = new JMenuItem("Open");
        jOpenMenu.addActionListener(e -> {
            FileDialog dialog = new FileDialog(jFrame, "Select a File to Open");
            dialog.setMode(FileDialog.LOAD);
            dialog.setFilenameFilter((dir, name) -> name.endsWith(".sm"));
            dialog.setVisible(true);

            String file = dialog.getFile();
            if (file != null)
                fromFile(new File(dialog.getDirectory(), file), true);
        });
        jFileMenu.add(jOpenMenu);
        JMenuItem jSaveMenu = new JMenuItem("Save");
        jSaveMenu.addActionListener(e -> toFile(false));
        jFileMenu.add(jSaveMenu);
        JMenuItem jSaveAsMenu = new JMenuItem("Save As...");
        jSaveAsMenu.addActionListener(e -> toFile(true));
        jFileMenu.add(jSaveAsMenu);

        jFileMenu.addSeparator();

        JMenuItem jImportMenu = new JMenuItem("Import Screenshot");
        jImportMenu.addActionListener(e -> importFile());
        jFileMenu.add(jImportMenu);
        JMenuItem jExportMenu = new JMenuItem("Export Image");
        jExportMenu.addActionListener(e -> exportFile());
        jFileMenu.add(jExportMenu);

        jMenuBar.add(jFileMenu);

        JMenu jHelpMenu = new JMenu("Help");

        JMenuItem jDesktopMenu = new JMenuItem("Create Desktop Entry");
        jDesktopMenu.addActionListener(e -> {
            try {
                new ProcessBuilder(
                        "sudo",
                        System.getProperty("java.home") + "/bin/java",
                        "-classpath",
                        Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                                .map(URL::getFile)
                                .collect(Collectors.joining(File.pathSeparator)),
                        DesktopEntryCreator.class.getCanonicalName()
                ).inheritIO().start();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        jHelpMenu.add(jDesktopMenu);
        JMenuItem jAboutMenu = new JMenuItem("About");
        jAboutMenu.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://jfenn.me/about/?ScreenshotMaker"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        jHelpMenu.add(jAboutMenu);
        JMenuItem jReportMenu = new JMenuItem("Report an Issue");
        jReportMenu.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://github.com/TheAndroidMaster/ScreenshotMaker/issues/new"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        jHelpMenu.add(jReportMenu);

        jMenuBar.add(jHelpMenu);

        // ------------------- Drawing Elements ----------------- //

        jScreenshotViewer = new JScreenshotViewer();

        // --------------------- Input Elements ----------------- //

        JPanel jInputPanel = new JPanel();
        jInputPanel.setLayout(new GridLayout(0, 2, 10, 8));
        jInputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        jInputPanel.add(new JLabel("Title"));
        jTitleTextField = new JTextField("Title", 10);
        jTitleTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                jScreenshotViewer.setTitle(jTitleTextField.getText());
                onValueChange();
            }
        });
        jInputPanel.add(jTitleTextField);

        jInputPanel.add(new JLabel("Description"));
        jDescriptionTextField = new JTextField("Description", 10);
        jDescriptionTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                jScreenshotViewer.setDescription(jDescriptionTextField.getText());
                onValueChange();
            }
        });
        jInputPanel.add(jDescriptionTextField);

        jInputPanel.add(new JLabel("Text Size"));
        jTextSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 50, 1));
        jTextSizeSpinner.addChangeListener(e -> {
            jScreenshotViewer.setTextSize((int) jTextSizeSpinner.getValue());
            onValueChange();
        });
        jInputPanel.add(jTextSizeSpinner);

        jInputPanel.add(new JLabel("Text Position"));
        jTextPositionComboBox = new JComboBox<>(new String[]{"Above", "Below"});
        jTextPositionComboBox.addActionListener(e -> {
            jScreenshotViewer.setPosition(jTextPositionComboBox.getSelectedIndex() == 1);
            onValueChange();
        });
        jInputPanel.add(jTextPositionComboBox);

        jInputPanel.add(new JLabel("Offset (% / 100)"));
        jOffsetSpinner = new JSpinner(new SpinnerNumberModel(0, -10, 100, 1));
        jOffsetSpinner.addChangeListener(e -> {
            jScreenshotViewer.setOffset((float) (int) jOffsetSpinner.getValue() / 100);
            onValueChange();
        });
        jInputPanel.add(jOffsetSpinner);

        jInputPanel.add(new JLabel("Text Color"));
        jTextColorChooserButton = new JColorChooserButton(Color.WHITE);
        jTextColorChooserButton.addColorListener(color -> {
            jScreenshotViewer.setTextColor(color);
            onValueChange();
        });
        jInputPanel.add(jTextColorChooserButton);

        jInputPanel.add(new JLabel("Background Color"));
        jBackgroundColorChooserButton = new JColorChooserButton(Color.BLACK);
        jBackgroundColorChooserButton.addColorListener(color -> {
            jScreenshotViewer.setBackgroundColor(color);
            onValueChange();
        });
        jInputPanel.add(jBackgroundColorChooserButton);

        jInputPanel.add(new JLabel("Export Size"));
        jExportSizeComboBox = new JComboBox<>(new String[]{"720x1280", "1080x1920"});
        jExportSizeComboBox.setSelectedIndex(1);
        jExportSizeComboBox.addActionListener(e -> {
            switch (jExportSizeComboBox.getSelectedIndex()) {
                case 0:
                    jScreenshotViewer.setExportSize(1280);
                    break;
                case 1:
                    jScreenshotViewer.setExportSize(1920);
                    break;
            }
            onValueChange();
        });
        jInputPanel.add(jExportSizeComboBox);

        jImportButton = new JButton("Import Screenshot");
        jImportButton.addActionListener(e -> importFile());
        jInputPanel.add(jImportButton);

        jExportButton = new JButton("Export Image");
        jExportButton.addActionListener(e -> exportFile());
        jInputPanel.add(jExportButton);

        // ------------------------------------------------------ //

        Container contentPane = jFrame.getContentPane();
        contentPane.add(BorderLayout.NORTH, jMenuBar);
        contentPane.add(BorderLayout.WEST, jScreenshotViewer);
        contentPane.add(BorderLayout.EAST, jInputPanel);

        jFrame.pack();
        jFrame.setLocation((screenSize.width / 2) - (jFrame.getWidth() / 2), (screenSize.height / 2) - (jFrame.getHeight() / 2));
        jFrame.setVisible(true);
    }

    private void onValueChange() {
        isChanged = true;
        if (file != null)
            jFrame.setTitle(file.getName() + " *");
    }

    private void newProcess(String... args) {
        String[] command = new String[args.length + 4];
        command[0] = System.getProperty("java.home") + "/bin/java";
        command[1] = "-classpath";
        command[2] = Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                .map(URL::getFile)
                .collect(Collectors.joining(File.pathSeparator));
        command[3] = getClass().getCanonicalName();
        System.arraycopy(args, 0, command, 4, command.length - 4);

        try {
            new ProcessBuilder(command).inheritIO().start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void importFile() {
        FileDialog dialog = new FileDialog(jFrame, "Choose a Screenshot");
        dialog.setMode(FileDialog.LOAD);
        dialog.setFilenameFilter((dir, name) -> name.endsWith(".png") || name.endsWith("jpg") || name.endsWith("jpeg"));
        dialog.setVisible(true);

        String file = dialog.getFile();
        if (file != null) {
            try {
                jScreenshotViewer.setScreenshot(new File(dialog.getDirectory(), file));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void exportFile() {
        FileDialog dialog = new FileDialog(jFrame, "Export To...");
        dialog.setMode(FileDialog.SAVE);
        dialog.setFilenameFilter((dir, name) -> name.endsWith("png"));
        dialog.setVisible(true);

        String file = dialog.getFile();
        if (file != null)
            jScreenshotViewer.toFile(new File(dialog.getDirectory(), file));
    }

    private void fromFile(File file, boolean newWindow) {
        if (file != null && file.exists() && file.getName().endsWith(".sm")) {
            if (newWindow) {
                boolean isTempChanged = false;
                if (isChanged) {
                    isTempChanged = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                            null,
                            "Would you like to open this file in a new window?",
                            "Open File",
                            JOptionPane.YES_NO_OPTION
                    );
                }

                if (isTempChanged) {
                    newProcess(file.getAbsolutePath());
                    return;
                } else {
                    jFrame.setVisible(false);
                    init();
                }
            }

            Scanner scanner = null;

            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (scanner != null) {
                try {
                    String title = scanner.nextLine();
                    jTitleTextField.setText(title);
                    jScreenshotViewer.setTitle(title);
                    String description = scanner.nextLine();
                    jDescriptionTextField.setText(description);
                    jScreenshotViewer.setDescription(description);
                    int textSize = scanner.nextInt();
                    jTextSizeSpinner.setValue(textSize);
                    jScreenshotViewer.setTextSize(textSize);
                    int position = scanner.nextInt();
                    jTextPositionComboBox.setSelectedIndex(position);
                    jScreenshotViewer.setPosition(position == 1);
                    int offset = scanner.nextInt();
                    jOffsetSpinner.setValue(offset);
                    jScreenshotViewer.setOffset((float) offset / 100);
                    Color textColor = new Color(scanner.nextInt());
                    jTextColorChooserButton.setColor(textColor);
                    jScreenshotViewer.setTextColor(textColor);
                    Color backgroundColor = new Color(scanner.nextInt());
                    jBackgroundColorChooserButton.setColor(backgroundColor);
                    jScreenshotViewer.setBackgroundColor(backgroundColor);
                    int exportSize = scanner.nextInt();
                    jExportSizeComboBox.setSelectedIndex(exportSize);
                    switch (exportSize) {
                        case 0:
                            jScreenshotViewer.setExportSize(1280);
                            break;
                        case 1:
                            jScreenshotViewer.setExportSize(1920);
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                scanner.close();
                this.file = file;
                jFrame.setTitle(file.getName());
                isChanged = false;
            }
        }
    }

    private void toFile(boolean ignoreFile) {
        File file = this.file;
        if (file == null || ignoreFile) {
            FileDialog dialog = new FileDialog(jFrame, "Save As...");
            dialog.setMode(FileDialog.SAVE);
            dialog.setFilenameFilter((dir, name) -> name.endsWith(".sm"));
            dialog.setVisible(true);

            String fileString = dialog.getFile();
            if (fileString != null)
                file = new File(dialog.getDirectory(), fileString);
            else return;
        }

        if (file.getName().endsWith(".sm")) {
            PrintWriter writer = null;

            try {
                writer = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (writer != null) {
                writer.println(jTitleTextField.getText());
                writer.println(jDescriptionTextField.getText());
                writer.println((int) jTextSizeSpinner.getValue());
                writer.println(jTextPositionComboBox.getSelectedIndex());
                writer.println((int) jOffsetSpinner.getValue());
                writer.println(jTextColorChooserButton.getColor().getRGB());
                writer.println(jBackgroundColorChooserButton.getColor().getRGB());
                writer.println(jExportSizeComboBox.getSelectedIndex());

                writer.close();
                this.file = file;
                jFrame.setTitle(file.getName());
            }
        }
    }

    public static void main(String... args) {
        ScreenshotMaker maker = new ScreenshotMaker();
        if (args.length > 0 && args[0] != null) {
            maker.fromFile(new File(args[0]), false);
            Point point = maker.jFrame.getLocation();
            point.setLocation(point.x + 20, point.y + 20);
            maker.jFrame.setLocation(point);
        }
    }
}
