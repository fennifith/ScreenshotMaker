package me.jfenn.screenshotmaker.data;

import com.sun.istack.internal.Nullable;
import me.jfenn.screenshotmaker.interfaces.Nameable;
import me.jfenn.screenshotmaker.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FrameData implements Nameable {

    public static final FrameData[] DEFAULTS = new FrameData[]{
            new FrameData("Pixel 2", "/assets/pixel_2_frame.png", 140, 300, 0, "16:9"),
            new FrameData("Nexus 5", "/assets/nexus_5_frame.png", 12, 58, -1, "16:9"),
            new FrameData("Galaxy Nexus", "/assets/galaxy_nexus_frame.png", 39, 86, -1, "17:10"),
            new FrameData("Alcatel", "/assets/alcatel_frame.png", 22, 81, -2, "16:9")
    };

    public static final int[] EXPORT_SIZES = new int[]{720, 1080, 1280, 1440, 1860, 2940};

    private String name;
    private File file;
    private String asset;
    private int frameSide;
    private int frameTop;
    private int offsetX;
    private String ratio;

    private BufferedImage image;
    private Map<Integer, BufferedImage> images;

    public FrameData(String name, String asset, int frameSide, int frameTop, int offsetX, String ratio) {
        this.name = name;
        this.asset = asset;
        this.frameSide = frameSide;
        this.frameTop = frameTop;
        this.offsetX = offsetX;
        images = new HashMap<>();
        this.ratio = ratio.replace("/", ":");
        if (!this.ratio.contains(":"))
            this.ratio += ":1";
    }

    public FrameData(String name, File file, int frameSide, int frameTop, int offsetX, String ratio) {
        this.name = name;
        this.file = file;
        this.frameSide = frameSide;
        this.frameTop = frameTop;
        this.offsetX = offsetX;
        images = new HashMap<>();
        this.ratio = ratio.replace("/", ":");
        if (!this.ratio.contains(":"))
            this.ratio += ":1";
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
            BufferedImage frame = getFrame();
            if (frame != null) {
                BufferedImage resizedImage = ImageUtils.progressiveResize(frame, width);
                images.put(width, resizedImage);
                return resizedImage;
            } else return null;
        }
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public int getSide() {
        return frameSide;
    }

    public int getTop() {
        return frameTop;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public Integer getWidth() {
        return image != null ? image.getWidth() : null;
    }

    public Integer getHeight() {
        return image != null ? image.getHeight() : null;
    }

    public void setSide(int side) {
        frameSide = side;
    }

    public void setTop(int top) {
        frameTop = top;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio.replace("/", ":");
        if (!this.ratio.contains(":"))
            this.ratio += ":1";
    }

    public float getRatio() {
        String[] numbers = ratio.split(":");
        return Float.parseFloat(numbers[0]) / Float.parseFloat(numbers[1]);
    }

    public String getRatioString() {
        return ratio;
    }

    public String[] getExportSizes() {
        String[] sizes = new String[EXPORT_SIZES.length];
        for (int i = 0; i < EXPORT_SIZES.length; i++) {
            sizes[i] = EXPORT_SIZES[i] + "x" + (int) (EXPORT_SIZES[i] * getRatio());
        }
        return sizes;
    }

    @Override
    public String toString() {
        return name + "," + file.getAbsolutePath() + "," + frameSide + "," + frameTop + "," + ratio;
    }

    @Nullable
    public static FrameData fromString(String string) {
        String[] values = string.split(",");
        if (values.length > 3) {
            int offsetX = 0;
            String ratio = "16:9";
            if (values.length > 4)
                offsetX = Integer.parseInt(values[4]);
            if (values.length > 5)
                ratio = values[5];

            try {
                return new FrameData(values[0], new File(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]), offsetX, ratio);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FrameData && name.equals(((FrameData) obj).name);
    }
}
