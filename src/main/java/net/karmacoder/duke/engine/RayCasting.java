package net.karmacoder.duke.engine;

import net.karmacoder.duke.image.Image;
import net.karmacoder.duke.image.SinglePixelImage;
import net.karmacoder.duke.math.VectorMath;
import net.karmacoder.duke.samples.Images;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static net.karmacoder.duke.math.VectorMath.*;

public class RayCasting implements Engine<RayCasting.Level, RayCasting.Input> {

  public static class Level {
    public final List<Integer> cells;
    public final List<Image> assets;
    public final Player initialPlayer;
    public final int width;
    public final int height;

    public Level(Player initialPlayer, int width, int height, List<Integer> cells, List<Image> assets) {
      this.initialPlayer = initialPlayer;
      this.width = width;
      this.height = height;
      this.cells = new ArrayList<>(cells);
      this.assets = new ArrayList<>(assets);
    }

    public static Level fromFile(String path) throws IOException {
      final var lines = Files.readAllLines(Path.of(path));

      final var player = getPlayer(lines);
      final var size = getLevelDimension(lines);
      final var cells = getCells(lines, size[0], size[1]);
      final var assets = getAssets(lines);

      return new Level(player, size[0], size[1], cells, assets);
    }

    private static Player getPlayer(List<String> lines) throws IOException {
      final var playerLine = lines.remove(0);

      final var split = playerLine.split(",");
      if (split.length != 3) {
        throw new IOException("Cannot parse player line.");
      }

      final var x = Double.parseDouble(split[0]);
      final var y = Double.parseDouble(split[1]);
      final var a = Double.parseDouble(split[2]);

      final var pos = new V(x, y);
      final var dir = VectorMath.times(VectorMath.rot(VectorMath.dtor(a)), new V(0, 1));

      return new Player(pos, dir);
    }

    private static int[] getLevelDimension(List<String> lines) throws IOException {
      final var dimensionLine = lines.remove(0);
      final var size = dimensionLine.split(",");

      if (size.length != 2) {
        throw new IOException("dimension of level is wrong.");
      }

      return new int[]{parseInt(size[0]), parseInt(size[1])};
    }

    private static List<Integer> getCells(List<String> lines, int width, int height) throws IOException {
      final List<Integer> result = new ArrayList<>(width * height);
      for (int i = 0; i < height; ++i) {
        final var line = lines.remove(0);
        if (line.length() < width) {
          throw new IOException("Level Line is to small: Line " + i + " needs to be atleast " + width + " characters long");
        }

        for (int x = 0; x < width; ++x) {
          result.add(line.charAt(x) - '0');
        }
      }

      return result;
    }

    private static List<Image> getAssets(List<String> lines) throws IOException {
      final List<Image> result = new ArrayList<>(lines.size());
      for (final var asset : lines) {
        if (asset.length() <= 0) {
          throw new IOException(asset + " is not a valid asset description");
        }

        final Image image;
        if (asset.startsWith("#")) {
          image = new SinglePixelImage(stringToRgb(asset.substring(1)));
        } else {
          System.out.println(asset);
          image = Images.fromBufferedImage(ImageIO.read(new File(asset)));
        }
        result.add(image);
      }

      return result;
    }

    private static int stringToRgb(String rgb) {
      return Integer.parseInt(rgb, 16);
    }
  }

  public static class Player {
    public V pos;
    public V dir;

    public Player(V pos, V dir) {
      this.pos = pos;
      this.dir = dir;
    }
  }

  public static class Input {

    public static class Camera {
      double distance;
      double width;

      public Camera(double distance, double width) {
        this.distance = distance;
        this.width = width;
      }
    }

    public Player player;
    public Camera camera;

    public Input(Player player, Camera camera) {
      this.player = player;
      this.camera = camera;
    }
  }

  public static class Screen {
    int width;
    int height;

    public Screen(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }

  private Screen screen;
  private Level level;
  private Input input;

  public RayCasting(Screen screen) {
    this.screen = screen;
  }

  @Override
  public boolean loadLevel(Level level) {
    if (level == null) return false;

    this.level = level;
    return true;
  }

  @Override
  public boolean processInput(Input input) {
    if (input == null) return false;

    this.input = input;
    return true;
  }

  @Override
  public Image renderFrame() {

    final List<Hit> hits = new ArrayList<>(screen.width);
    castAllColumns(hits);

    final int[] pixels = new int[screen.width * screen.height];

    for (int y = 0; y < screen.height; ++y) {
      for (int x = 0; x < screen.width; ++x) {
        pixels[x + y * screen.width] = rayCast(x, y, hits, screen);
      }
    }

    return new Image() {
      @Override
      public int[] getPixels() {
        return pixels;
      }

      @Override
      public int getWidth() {
        return screen.width;
      }

      @Override
      public int getHeight() {
        return screen.height;
      }
    };
  }

  class Hit {
    V coordinate;
    V direction;
    int cell;
    double distance;

    public Hit(V coordinate, V direction, int cell, double distance) {
      this.coordinate = coordinate;
      this.direction = direction;
      this.cell = cell;
      this.distance = distance;
    }
  }

  void castAllColumns(List<Hit> hits) {
    V playerToPlane = times(input.player.dir, input.camera.distance);
    V planeDir = times(rot(dtor(90.0)), input.player.dir);
    V planeStart = vplus(
        input.player.pos, vplus(playerToPlane, times(planeDir, input.camera.width / 2.0)));

    for (int x = 0; x < screen.width; ++x) {
      V rayOnPlane =
          vplus(planeStart,
              times(times(rot(dtor(180.0)), planeDir),
                  input.camera.width * (1.0 - (double) x / screen.width)));
      V rayDir = vminus(rayOnPlane, input.player.pos);

      boolean hit = false;
      for (double distance = 0.01; distance < 100. && !hit; distance += 0.01) {
        V check = vplus(input.player.pos, times(rayDir, distance));
        if (((int) check.x) >= 0 && ((int) check.x) < level.width &&
            ((int) check.y) >= 0 && ((int) check.y) < level.height) {
          int cell = level.cells.get(((int) check.y * level.width) + ((int) check.x));

          if (cell > 0) {
            hits.add(new Hit(check, rayDir, cell, distance));

            hit = true;
          }
        } else {
          break;
        }
      }

      if (!hit) {
        hits.add(null);
      }
    }
  }

  int rayCast(int x, int y, List<Hit> hits, Screen screen) {
    final Hit hit = hits.get(x);
    if (hit == null) {
      return drawCeiling();
    }

    double distance = hit.distance;
    double wallheight = Math.min(1.0 / distance, screen.height);
    double horizon = screen.height / 2.0;

    if (y > screen.height / 2. - wallheight / 2. &&
        y < screen.height / 2. + wallheight / 2.) {
      return drawHit(hit, y - (screen.height / 2. - wallheight / 2.), wallheight);
    } else {
      // remember: Drawing starts from top
      if (y >= screen.height / 2.) {
        return drawFloor();
      } else {
        return drawCeiling();
      }
    }
  }

  int drawHit(Hit hit, double y, double height) {
    double u = abs((hit.coordinate.x + hit.coordinate.y) % 1.0);
    double v = y / height;
    v = VectorMath.clamp(v, 0., 0.999);
    Image asset = level.assets.get(hit.cell - 1); // assets start from 1 on the map

    int tx = (int) (asset.getWidth() * u);
    int ty = (int) (asset.getHeight() * v);
    return asset.getPixels()[tx + ty * asset.getWidth()];
  }

  int drawCeiling() {
    return 0x0000FF;
  }

  int drawFloor() {
    return 0x00FF00;
  }
}
