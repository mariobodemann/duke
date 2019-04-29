package net.karmacoder.duke.console;

import net.karmacoder.duke.display.Displayer;
import net.karmacoder.duke.image.Image;

import java.io.PrintStream;

import static java.lang.Math.round;

public class Console implements Displayer {
  private final PrintStream out;

  private Console(PrintStream out) {
    this.out = out;
  }

  public Console() {
    this(System.out);
  }

  private final String END_COLORIZATION = "\u001B[m";
  private final String TWO_ROWS_FORMAT = "\u001B[38;5;%d;48;5;%dm▀";

  @Override
  public void display(Image image) {
    final int[] pixels = image.getPixels();
    for (int row = 0; row < image.getHeight(); row += 2) {
      for (int column = 0; column < image.getWidth(); column++) {
        final int topHalfColor = getPixelAt(pixels, column, row, image.getWidth(), image.getHeight());
        final int bottomHalfColor = getPixelAt(pixels, column, row + 1, image.getWidth(), image.getHeight());

        out.printf(TWO_ROWS_FORMAT, rgbTo256(topHalfColor), rgbTo256(bottomHalfColor));
      }
      out.println(END_COLORIZATION);
    }
  }

  private int getPixelAt(int[] pixels, int x, int y, int width, int height) {
    if (x >= 0 && x < width &&
        y >= 0 && y < height) {
      return pixels[x + (y * width)];
    } else {
      return -1;
    }
  }

  private int rgbTo256(int rgb) {
    return 16 +
        36 * round((r(rgb) / 256f) * 5.0f) +
        6 * round((g(rgb) / 256f) * 5.0f) +
        round((b(rgb) / 256f) * 5.0f);
  }

  private int r(int color) {
    return (color >> 16) & 0xFF;
  }

  private int g(int color) {
    return (color >> 8) & 0xFF;
  }

  private int b(int color) {
    return color & 0xFF;
  }
}
