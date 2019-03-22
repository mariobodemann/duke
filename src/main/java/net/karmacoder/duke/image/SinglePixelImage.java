package net.karmacoder.duke.image;

public class SinglePixelImage implements Image {
  private final int color;

  public SinglePixelImage(int color) {
    this.color = color;
  }

  @Override
  public int[] getPixels() {
    return new int[]{color};
  }

  @Override
  public int getWidth() {
    return 1;
  }

  @Override
  public int getHeight() {
    return 1;
  }
}
