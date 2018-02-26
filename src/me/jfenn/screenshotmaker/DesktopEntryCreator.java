package me.jfenn.screenshotmaker;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DesktopEntryCreator {

    public static void main(String... args) {
        File file = new File("/usr/share/applications/screenshotmaker.desktop");
        PrintWriter writer;

        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage(), e1.getClass().getName(), JOptionPane.WARNING_MESSAGE);
            return;
        }

        writer.println("[Desktop Entry]");
        writer.println("Version=1.0");
        writer.println("Name=Screenshot Maker");
        writer.println("Comment=A small applet used to generate Play Store screenshots.");
        writer.println("Exec="
                + System.getProperty("java.home")
                + "/bin/java -classpath "
                + Arrays.stream(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs())
                .map(URL::getFile)
                .collect(Collectors.joining(File.pathSeparator))
                + " "
                + ScreenshotMaker.class.getCanonicalName()
        );
        writer.println("Type=Application");
        writer.println("Terminal=false");
        writer.close();

        try {
            Runtime.getRuntime().exec("chmod +x /usr/share/applications/screenshotmaker.desktop");
        } catch (IOException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, e1.getMessage(), e1.getClass().getName(), JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, "Desktop entry created successfully.", "Desktop Entry", JOptionPane.INFORMATION_MESSAGE);
    }

}
