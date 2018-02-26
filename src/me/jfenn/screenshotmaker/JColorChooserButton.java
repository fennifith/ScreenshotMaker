package me.jfenn.screenshotmaker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class JColorChooserButton extends JButton implements ActionListener {

    private Color color;
    private List<ColorListener> listeners;

    public JColorChooserButton(Color color) {
        listeners = new ArrayList<>();
        setColor(color);
        addActionListener(this);
    }

    public void addColorListener(ColorListener listener) {
        listeners.add(listener);
    }

    public void removeColorListener(ColorListener listener) {
        listeners.remove(listener);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color != null && !color.equals(this.color)) {
            this.color = color;

            for (ColorListener listener : listeners) {
                listener.colorChanged(color);
            }

            setIcon(createIcon(color, 16, 16));
            setText(String.format("#%06X", (0xFFFFFF & color.getRGB())));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setColor(JColorChooser.showDialog(null, "Choose a color", color));
    }

    private static ImageIcon createIcon(Color main, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.BLACK);
        graphics.drawRect(0, 0, width-1, height-1);
        image.flush();
        return new ImageIcon(image);
    }

    public interface ColorListener {
        void colorChanged(Color color);
    }
}
