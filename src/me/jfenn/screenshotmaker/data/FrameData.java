package me.jfenn.screenshotmaker.data;

import com.sun.istack.internal.Nullable;
import me.jfenn.screenshotmaker.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FrameData {

    public static FrameData[] DEFAULTS = new FrameData[]{
            new FrameData("Pixel 2", "/assets/pixel_2_frame.png", 140, 300)
    };

    private String name;
    private File file;
    private String asset;
    private int frameSide;
    private int frameTop;

    private BufferedImage image;
    private Map<Integer, BufferedImage> images;

    public FrameData(String name, String asset, int frameSide, int frameTop) {
        this.name = name;
        this.asset = asset;
        this.frameSide = frameSide;
        this.frameTop = frameTop;
        images = new HashMap<>();
    }

    public FrameData(String name, File file, int frameSide, int frameTop) {
        this.name = name;
        this.file = file;
        this.frameSide = frameSide;
        this.frameTop = frameTop;
        images = new HashMap<>();
    }

    @Nullable
    private BufferedImage getFrame() {
        if (image == null) {
            try {
                if (asset != null)
                    image = ImageIO.read(FrameData.class.getResourceAsStream(asset));
                else if (file != null)
                    image = ImageIO.read(file);
            } catch (IOException ignored) {
            }
        }

        if (image != null)
            images.put(image.getWidth(), image);
        return image;
    }

    @Nullable
    public BufferedImage getResizedFrame(int width) {
        if (images.containsKey(width))
            return images.get(width);
        else {
            BufferedImage resizedImage = ImageUtils.progressiveResize(getFrame(), width);
            images.put(width, resizedImage);
            return resizedImage;
        }
    }

    public int getSide() {
        return frameSide;
    }

    public int getTop() {
        return frameTop;
    }

    public Integer getWidth() {
        return image != null ? image.getWidth() : null;
    }

    public Integer getHeight() {
        return image != null ? image.getHeight() : null;
    }

    @Override
    public String toString() {
        return name + "," + file.getAbsolutePath() + "," + frameSide + "," + frameTop;
    }

    @Nullable
    public static FrameData fromString(String string) {
        String[] values = string.split(",");
        if (values.length > 3) {
            try {
                return new FrameData(values[0], new File(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}
