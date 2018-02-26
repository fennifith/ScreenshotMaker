package me.jfenn.screenshotmaker;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JScreenshotViewer extends JComponent {

    public static final int POSITION_ABOVE = 0;
    public static final int POSITION_BELOW = 1;

    private String title, description;
    private int textSize, position;
    private float offset;
    private Color textColor, backgroundColor;
    private int exportHeight;

    private BufferedImage frame, resizedFrame;
    int frameSide, frameTop;
    private BufferedImage screenshot, resizedScreenshot;

    public JScreenshotViewer() {
        title = "Title";
        description = "Description";
        textSize = 14;
        textColor = Color.WHITE;
        backgroundColor = Color.BLACK;
        exportHeight = 1920;

        try {
            frame = ImageIO.read(new File("assets", "pixel_2_frame.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        frameSide = 140;
        frameTop = 300;
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    public void setDescription(String description) {
        this.description = description;
        repaint();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        repaint();
    }

    public void setPosition(int position) {
        this.position = position;
        repaint();
    }

    public void setOffset(float offset) {
        this.offset = offset;
        repaint();
    }

    public void setTextColor(Color color) {
        textColor = color;
        repaint();
    }

    public void setBackgroundColor(Color color) {
        backgroundColor = color;
        repaint();
    }

    public void setExportSize(int height) {
        exportHeight = height;
    }

    public void setScreenshot(File file) throws IOException {
        screenshot = ImageIO.read(file);
        repaint();
    }

    public void toFile(File file) {
        BufferedImage image = new BufferedImage((exportHeight * 9) / 16, exportHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        paintScreenshot(graphics, exportHeight);

        try {
            if (ImageIO.write(image, "png", file)) {
                System.out.println("-- saved to " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintScreenshot(g, getHeight());
    }

    private void paintScreenshot(Graphics g, int height) {
        int width = (height * 9) / 16;
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        if (g instanceof Graphics2D) {
            float start = ((float) height / 10) + (offset * height / 4);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Roboto", Font.BOLD, (int) (((float) height / 300) * textSize)));
            g2.setColor(textColor);
            FontMetrics fontMetrics = g2.getFontMetrics();

            g2.drawString(title, ((float) width / 2) - ((float) fontMetrics.stringWidth(title) / 2), start);
            start += fontMetrics.getHeight();

            g2.setFont(new Font("Roboto", Font.PLAIN, (int) (((float) height / 400) * textSize)));
            fontMetrics = g2.getFontMetrics();

            g2.drawString(description, ((float) width / 2) - ((float) fontMetrics.stringWidth(description) / 2), start);
            start += fontMetrics.getHeight() + (offset * height / 4);

            if (frame != null && (resizedFrame == null || resizedFrame.getWidth() != (int) (width * 0.9))) {
                resizedFrame = progressiveResize(frame, (int) (width * 0.9));
            }

            if (resizedFrame != null) {
                int frameSide = this.frameSide * resizedFrame.getWidth() / frame.getWidth();
                int frameTop = this.frameTop * resizedFrame.getHeight() / frame.getHeight();

                g2.drawImage(resizedFrame, (width / 2) - (resizedFrame.getWidth() / 2), (int) start, null);

                if (screenshot != null && (resizedScreenshot == null || resizedScreenshot.getWidth() != resizedFrame.getWidth() - (frameSide * 2))) {
                    resizedScreenshot = progressiveResize(screenshot, resizedFrame.getWidth() - (frameSide * 2));
                }

                if (resizedScreenshot != null) {
                    g2.drawImage(resizedScreenshot, (width / 2) - (resizedScreenshot.getWidth() / 2), (int) start + frameTop, null);
                }
            }
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((getHeight() * 720) / 1280, getHeight());
    }

    private static BufferedImage progressiveResize(BufferedImage source, int width) {
        int height = width * source.getHeight() / source.getWidth();
        int w = Math.max(source.getWidth() / 2, width);
        int h = Math.max(source.getHeight() / 2, height);

        BufferedImage img = commonResize(source, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        while (w != width || h != height) {
            BufferedImage prev = img;
            w = Math.max(w / 2, width);
            h = Math.max(h / 2, height);
            img = commonResize(prev, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            prev.flush();
        }

        return img;
    }

    private static BufferedImage commonResize(BufferedImage source, int width, int height, Object hint) {
        BufferedImage img = new BufferedImage(width, height, source.getType());
        Graphics2D g = img.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g.drawImage(source, 0, 0, width, height, null);
        } catch (Exception ignored) {
        }

        g.dispose();
        return img;
    }

}
