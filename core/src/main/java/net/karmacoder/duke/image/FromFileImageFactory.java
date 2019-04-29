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
    final BufferedImage image = read(new File(path));
    final int width = (int) (image.getWidth() * scale);
    final int height = (int) (image.getHeight() * scale);
    return new ScaledBufferedImage(image, width, height);
  }

  @Override
  public Image create(String path, int width, int height) throws Exception {
    BufferedImage image = read(new File(path));
    return new ScaledBufferedImage(image, width, height);
  }

  private static class UnScaledBufferedImage extends Image {
    UnScaledBufferedImage(BufferedImage image) {
      super(
          image.getRGB(
              0, 0,
              image.getWidth(), image.getHeight(),
              null,
              0,
              image.getWidth()),
          image.getWidth(),
          image.getHeight());
    }
  }

  private static class ScaledBufferedImage extends Image {

    ScaledBufferedImage(BufferedImage image, int width, int height) {
      super(readData(image, width, height), width, height);
    }

    private static int[] readData(BufferedImage image, int width, int height) {
      final float aspectRatio = ((float) image.getWidth()) / image.getHeight();

      width = width <= 0 ? (int) (height * aspectRatio) : width;
      height = height <= 0 ? (int) (width / aspectRatio) : height;

      final BufferedImage scaledImage = new BufferedImage(width, height, TYPE_INT_RGB);
      Graphics2D graphics2D = scaledImage.createGraphics();
      graphics2D.drawImage(image, 0, 0, width, height, null);
      graphics2D.dispose();

      return scaledImage.getRGB(0, 0, width, height, null, 0, width);
    }
  }

}
