package me.jfenn.screenshotmaker.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static BufferedImage progressiveResize(BufferedImage source, int width) {
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
