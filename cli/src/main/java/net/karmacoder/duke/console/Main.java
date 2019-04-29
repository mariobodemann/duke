package net.karmacoder.duke.console;

import net.karmacoder.duke.engine.LevelImage;
import net.karmacoder.duke.engine.RayCasting;
import net.karmacoder.duke.engine.RayCasting.Input;
import net.karmacoder.duke.engine.RayCasting.Level;
import net.karmacoder.duke.engine.RayCasting.Screen;
import net.karmacoder.duke.image.FromFileImageFactory;
import net.karmacoder.duke.math.VectorMath;
import net.karmacoder.duke.math.VectorMath.M;
import net.karmacoder.duke.samples.DukeImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
  static class Settings {
    boolean scaled = false;
    float scale = 1.0f;
    int width = -1;
    int height = -1;
    boolean threeD = false;
    final List<String> files = new ArrayList<>();

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

        if (argument.equals("3d")) {
          settings.threeD = true;
        }

        if (new File(argument).exists()) {
          settings.files.add(argument);
        }
      }

      return settings;
    }
  }

  public static void main(String[] args) {
    final List<String> arguments = Arrays.asList(args);
    final Settings settings = Settings.fromArguments(arguments);

    if (arguments.size() == 0 || arguments.contains("--help") || arguments.contains("-h") || settings.files.isEmpty()) {
      showHelp();
      return;
    }

    if (settings.threeD) {
      try {
        threeD(settings);
      } catch (Exception e) {
        System.err.println("Error found, please check files provided.");
        e.printStackTrace(System.err);
      }
    } else {
      final FromFileImageFactory factory = new FromFileImageFactory();

      for (final String file : settings.files) {
        if (settings.files.size() > 1) {
          System.out.println(file);
        }

        try {
          if (settings.scaled) {
            new Console().display(factory.create(file, settings.scale));
          } else if (settings.width > 0 || settings.height > 0) {
            new Console().display(factory.create(file, settings.width, settings.height));
          } else {
            new Console().display(factory.create(file));
          }
        } catch (Exception e) {
          System.err.println("Error found, please check files provided. (" + file + ")");
          e.printStackTrace(System.err);
        }
      }
    }
  }

  private static void threeD(Settings settings) throws Exception {
    final Level level = Level.fromFile(settings.files.get(0), new FromFileImageFactory());

    final Input input = new Input(
        level.initialPlayer,
        new Input.Camera(10. * settings.scale, 10. * settings.scale)
    );

    final int width = settings.width < 0 ? 30 : settings.width;
    final int height = settings.height < 0 ? 40 : settings.height;

    final RayCasting engine = new RayCasting(new Screen(width, height));
    final Console display = new Console();

    engine.loadLevel(level);
    engine.processInput(input);

    System.out.println("Current level:");
    display.display(new LevelImage(level));

    final M rot36 = VectorMath.rot(VectorMath.dtor(3.6));
    input.player.pos = VectorMath.vplus(input.player.pos, VectorMath.times(input.player.dir, -2.));
    for (int i = 0; i < 100; i++) {
      input.player.dir = VectorMath.times(rot36, input.player.dir);

      engine.processInput(input);
      display.display(engine.renderFrame());
      moveUp(settings.height);
      Thread.sleep(10);
    }

    moveDown(settings.height);
  }

  private final static String MOVE_N_LINES_UP = "\u001B[%dA";
  private final static String MOVE_N_LINES_DOWN = "\u001B[%dB";

  private static void moveUp(int height) {
    System.out.printf(MOVE_N_LINES_UP, height / 2);
  }

  private static void moveDown(int height) {
    System.out.printf(MOVE_N_LINES_DOWN, height / 2);
  }

  private static void showHelp() {
    new Console().display(new DukeImage());
    System.out.println();
    System.out.println("Console image displayer: Display an image in the duke.console");
    System.out.println();
    System.out.println("Usage: duke [-h|--help] [--scale S=1.0] [--width W=-1] [--height H=-1] [FILE_1 [.. FILE_N]]");
    System.out.println();
    System.out.println("\tDisplay image files 0 to N in duke.console. If scale s is given, rescale all images by factor S.");
  }
}
