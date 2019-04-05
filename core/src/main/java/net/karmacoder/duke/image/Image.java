package net.karmacoder.duke.image;

public class Image {
  final int[] pixels;
  final int width;
  final int height;

  public Image(int[] pixels, int width, int height) {
    this.pixels = pixels;
    this.width = width;
    this.height = height;
  }

  public int[] getPixels() {
    return pixels;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
