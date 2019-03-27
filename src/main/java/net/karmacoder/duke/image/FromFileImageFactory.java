package net.karmacoder.duke.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.imageio.ImageIO.read;

public class FromFileImageFactory implements ImageFactory {
  @Override
  public Image create(String path) throws Exception {
    return new UnScaledBufferedImage(read(new File(path)));
  }

  @Override
  public Image create(String path, float scale) throws Exception {
    final var image = read(new File(path));
    final var width = (int) (image.getWidth() * scale);
    final var height = (int) (image.getHeight() * scale);
    return new ScaledBufferedImage(image, width, height);
  }

  @Override
  public Image create(String path, int width, int height) throws Exception {
    var image = read(new File(path));
    return new ScaledBufferedImage(image, width, height);
  }

  private static class UnScaledBufferedImage implements Image {
    private final int[] data;
    private final int width;
    private final int height;

    UnScaledBufferedImage(BufferedImage image) {
      this.width = image.getWidth();
      this.height = image.getHeight();
      data = image.getRGB(
          0, 0,
          getWidth(), getHeight(),
          null,
          0,
          getWidth());
    }

    @Override
    public int[] getPixels() {
      return data;
    }

    @Override
    public int getWidth() {
      return width;
    }

    @Override
    public int getHeight() {
      return height;
    }
  }

  private static class ScaledBufferedImage implements Image {
    private final int[] data;
    private final int width;
    private final int height;

    ScaledBufferedImage(BufferedImage image, int width, int height) {
      final var aspectRatio = ((float) image.getWidth()) / image.getHeight();

      this.width = width <= 0 ? (int) (height * aspectRatio) : width;
      this.height = height <= 0 ? (int) (width / aspectRatio) : height;

      final var scaledImage = new BufferedImage(getWidth(), getHeight(), TYPE_INT_RGB);
      Graphics2D graphics2D = scaledImage.createGraphics();
      graphics2D.drawImage(image, 0, 0, getWidth(), getHeight(), null);
      graphics2D.dispose();

      data = scaledImage.getRGB(0, 0, getWidth(), getHeight(), null, 0, getWidth());
    }

    @Override
    public int[] getPixels() {
      return data;
    }

    @Override
    public int getWidth() {
      return width;
    }

    @Override
    public int getHeight() {
      return height;
    }
  }

}
