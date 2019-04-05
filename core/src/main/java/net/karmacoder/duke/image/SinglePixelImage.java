package net.karmacoder.duke.image;

public class SinglePixelImage extends Image {
  public SinglePixelImage(int color) {
    super(new int[]{color}, 1, 1);
  }
}
