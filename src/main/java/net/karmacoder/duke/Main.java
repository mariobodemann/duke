package net.karmacoder.duke;

import net.karmacoder.duke.console.Console;
import net.karmacoder.duke.samples.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.karmacoder.duke.samples.Images.duke;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      showHelp();
    } else {
      boolean scaled = false;
      float scale = 1.0f;
      int width = -1;
      int height = -1;

      for (int i = 0; i < args.length; ++i) {
        final String option = args[i];
        if (option.equals("--scale") && args.length > i + 1) {
          final String stringSize = args[i + 1];
          scale = Float.parseFloat(stringSize);
          scaled = true;
        }

        if (option.equals("--width") && args.length > i + 1) {
          final String stringWidth = args[i + 1];
          width = Integer.parseInt(stringWidth);
        }

        if (option.equals("--height") && args.length > i + 1) {
          final String stringheight = args[i + 1];
          height = Integer.parseInt(stringheight);
        }
      }

      final List<String> files = Arrays.stream(args).filter(it -> new File(it).exists()).collect(Collectors.toList());
      for (final String file : files) {
        if (files.size() > 1) {
          System.out.println(file);
        }

        final BufferedImage img = ImageIO.read(new File(file));

        if (scaled) {
          new Console().display(Images.fromBufferedImage(img, scale));
        } else {
          new Console().display(Images.fromBufferedImage(img, width, height));
        }
      }
    }
  }

  private static void showHelp() {
    new Console().display(duke());
    System.out.println();
    System.out.println("Console image displayer: Display an image in the console");
    System.out.println();
    System.out.println("Usage: duke [--scale S=1.0] [--width W=-1] [--height H=-1] FILE_1 [.. FILE_N]");
    System.out.println();
    System.out.println("\tDisplay files 1 to N in console. If scale s is given, rescale all images by factor S.");
  }
}
