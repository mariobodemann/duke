package net.karmacoder.duke;

import net.karmacoder.duke.console.Console;
import net.karmacoder.duke.samples.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.karmacoder.duke.samples.Images.duke;

public class Main {
  public static class Settings {
    boolean scaled = false;
    float scale = 1.0f;
    int width = -1;
    int height = -1;
    List<String> files = new ArrayList<>();

    static Settings fromArguments(List<String> arguments) {
      final Settings settings = new Settings();
      for (int i = 0; i < arguments.size(); ++i) {
        final String argument = arguments.get(i);
        if (argument.equals("--scale") && arguments.size() > i + 1) {
          final String stringSize = arguments.get(i + 1);
          settings.scale = Float.parseFloat(stringSize);
          settings.scaled = true;
        }

        if (argument.equals("--width") && arguments.size() > i + 1) {
          settings.width = Integer.parseInt(arguments.get(i + 1));
        }

        if (argument.equals("--height") && arguments.size() > i + 1) {
          settings.height = Integer.parseInt(arguments.get(i + 1));
        }

        if (new File(argument).exists()) {
          settings.files.add(argument);
        }
      }

      return settings;
    }
  }

  public static void main(String[] args) throws IOException {
    final List<String> arguments = Arrays.asList(args);

    final Settings settings = Settings.fromArguments(arguments);

    if (arguments.size() == 0 || arguments.contains("--help") || arguments.contains("-h") || settings.files.size() == 0) {
      showHelp();
    } else {
      for (final String file : settings.files) {
        if (settings.files.size() > 1) {
          System.out.println(file);
        }

        final BufferedImage img = ImageIO.read(new File(file));

        if (settings.scaled) {
          new Console().display(Images.fromBufferedImage(img, settings.scale));
        } else if (settings.width > 0 || settings.height > 0) {
          new Console().display(Images.fromBufferedImage(img, settings.width, settings.height));
        } else {
          new Console().display(Images.fromBufferedImage(img));
        }
      }
    }
  }

  private static void showHelp() {
    new Console().display(duke());
    System.out.println();
    System.out.println("Console image displayer: Display an image in the console");
    System.out.println();
    System.out.println("Usage: duke [-h|--help] [--scale S=1.0] [--width W=-1] [--height H=-1] [FILE_1 [.. FILE_N]]");
    System.out.println();
    System.out.println("\tDisplay image files 0 to N in console. If scale s is given, rescale all images by factor S.");
  }
}
