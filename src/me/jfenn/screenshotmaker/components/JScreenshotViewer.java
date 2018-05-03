package me.jfenn.screenshotmaker.components;

import me.jfenn.screenshotmaker.data.FrameData;
import me.jfenn.screenshotmaker.utils.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JScreenshotViewer extends JComponent {

    private String title, description;
    private int textSize;
    private String textFont;
    private boolean reversePosition;
    private float offset;
    private Color textColor, backgroundColor;
    private int exportHeight;

    private FrameData frame;
    private BufferedImage screenshot, resizedScreenshot;

    public JScreenshotViewer() {
        title = "Title";
        description = "Description";
        textSize = 14;
        textFont = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0];
        textColor = Color.WHITE;
        backgroundColor = Color.BLACK;
        exportHeight = 1920;

        frame = FrameData.DEFAULTS[0];
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

    public void setTextFont(String textFont) {
        this.textFont = textFont;
        repaint();
    }

    public void setPosition(boolean reversePosition) {
        this.reversePosition = reversePosition;
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

    public void setExportSize(int exportSize) {
        switch (exportSize) {
            case 0:
                exportHeight = 1280;
                break;
            case 1:
                exportHeight = 1920;
                break;
        }
    }

    public void setScreenshot(File file) throws IOException {
        screenshot = ImageIO.read(file);
        repaint();
    }

    public void setFrame(FrameData frame) {
        this.frame = frame;
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
        paintScreenshot(g, Math.min(getHeight(), (int) (getWidth() * frame.getRatio())));
    }

    private void paintScreenshot(Graphics g, int height) {
        int width = (int) ((float) height / frame.getRatio());
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        if (g instanceof Graphics2D) {
            float start = ((float) height / 10) + (offset * height / 4);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(textColor);

            FontMetrics fontMetrics;

            if (!reversePosition) {
                g2.setFont(new Font(textFont, Font.BOLD, (int) (((float) height / 300) * textSize)));
                fontMetrics = g2.getFontMetrics();

                g2.drawString(title, ((float) width / 2) - ((float) fontMetrics.stringWidth(title) / 2), start);
                start += fontMetrics.getHeight();
            }

            g2.setFont(new Font(textFont, Font.PLAIN, (int) (((float) height / 400) * textSize)));
            fontMetrics = g2.getFontMetrics();

            g2.drawString(description, ((float) width / 2) - ((float) fontMetrics.stringWidth(description) / 2), reversePosition ? height - start : start);
            start += fontMetrics.getHeight();

            if (reversePosition) {
                g2.setFont(new Font(textFont, Font.BOLD, (int) (((float) height / 300) * textSize)));
                fontMetrics = g2.getFontMetrics();

                g2.drawString(title, ((float) width / 2) - ((float) fontMetrics.stringWidth(title) / 2), height - start);
                start += fontMetrics.getHeight();
            }

            BufferedImage resizedFrame = frame.getResizedFrame((int) (width * 0.9));

            if (resizedFrame != null) {
                start += (offset * height / 4);

                int frameSide = frame.getSide() * resizedFrame.getWidth() / frame.getWidth();
                int frameTop = frame.getTop() * resizedFrame.getHeight() / frame.getHeight();

                g2.drawImage(resizedFrame, (width / 2) - (resizedFrame.getWidth() / 2), (int) (reversePosition ? height - start - resizedFrame.getHeight() : start), null);

                if (screenshot != null && (resizedScreenshot == null || resizedScreenshot.getWidth() != resizedFrame.getWidth() - (frameSide * 2))) {
                    resizedScreenshot = ImageUtils.progressiveResize(screenshot, resizedFrame.getWidth() - (frameSide * 2));
                }

                int screenshotWidth = resizedFrame.getWidth() - (frameSide * 2);
                int screenshotHeight = (int) (screenshotWidth * frame.getRatio());
                start += reversePosition ? resizedFrame.getHeight() - screenshotHeight - frameTop : frameTop;

                if (resizedScreenshot != null) {
                    g2.drawImage(resizedScreenshot, (width / 2) - (resizedScreenshot.getWidth() / 2) + frame.getOffsetX(), (int) (reversePosition ? height - start - resizedScreenshot.getHeight() : start), null);
                } else {
                    g2.setColor(new Color(255, 255, 255, 150));
                    g2.fillRect((width / 2) - (screenshotWidth / 2) + frame.getOffsetX(), (int) (reversePosition ? height - start - screenshotHeight : start), screenshotWidth, screenshotHeight);
                    g2.setColor(new Color(0, 0, 0, 150));
                    g2.drawRect((width / 2) - (screenshotWidth / 2) + frame.getOffsetX(), (int) (reversePosition ? height - start - screenshotHeight : start), screenshotWidth, screenshotHeight);
                }
            }
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((getHeight() * 720) / 1280, getHeight());
    }

    @Override
    public Dimension getSize(Dimension rv) {
        rv.setSize((getHeight() * 720) / 1280, getHeight());
        return rv;
    }

    @Override
    public Dimension getSize() {
        return new Dimension((getHeight() * 720) / 1280, getHeight());
    }

}
