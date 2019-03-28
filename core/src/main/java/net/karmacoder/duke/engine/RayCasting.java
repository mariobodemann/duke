package net.karmacoder.duke.engine;

import net.karmacoder.duke.image.Image;
import net.karmacoder.duke.image.ImageFactory;
import net.karmacoder.duke.image.SinglePixelImage;
import net.karmacoder.duke.math.VectorMath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

public class RayCasting implements Engine<RayCasting.Level, RayCasting.Input> {

  public static class Level {
    public final List<Integer> cells;
    public final List<Image> assets;
    public final Player initialPlayer;
    public final int width;
    public final int height;

    Level(Player initialPlayer, int width, int height, List<Integer> cells, List<Image> assets) {
      this.initialPlayer = initialPlayer;
      this.width = width;
      this.height = height;
      this.cells = new ArrayList<>(cells);
      this.assets = new ArrayList<>(assets);
    }

    public static Level fromFile(String path, ImageFactory factory) throws Exception {
      final var lines = Files.readAllLines(Path.of(path));

      final var player = getPlayer(lines);
      final var size = getLevelDimension(lines);
      final var cells = getCells(lines, size[0], size[1]);
      final var assets = getAssets(factory, lines);

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

      final var pos = new VectorMath.V(x, y);
      final var dir = VectorMath.times(VectorMath.rot(VectorMath.dtor(a)), new VectorMath.V(0, 1));

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
      for (var i = 0; i < height; ++i) {
        final var line = lines.remove(0);
        if (line.length() < width) {
          throw new IOException("Level Line is to small: Line " + i + " needs to be atleast " + width + " characters long");
        }

        for (var x = 0; x < width; ++x) {
          result.add(line.charAt(x) - '0');
        }
      }

      return result;
    }

    private static List<Image> getAssets(ImageFactory factory, List<String> lines) throws Exception {
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
          image = factory.create(asset);
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
    final VectorMath.V pos;
    public VectorMath.V dir;

    Player(VectorMath.V pos, VectorMath.V dir) {
      this.pos = pos;
      this.dir = dir;
    }
  }

  public static class Input {

    public static class Camera {
      final double distance;
      final double width;

      public Camera(double distance, double width) {
        this.distance = distance;
        this.width = width;
      }
    }

    public final Player player;
    final Camera camera;

    public Input(Player player, Camera camera) {
      this.player = player;
      this.camera = camera;
    }
  }

  public static class Screen {
    final int width;
    final int height;

    public Screen(int width, int height) {
      this.width = width;
      this.height = height;
    }
  }

  private final Screen screen;
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

    final var hits = new ArrayList<Hit>(screen.width);
    castAllColumns(hits);

    final var pixels = new int[screen.width * screen.height];

    for (var y = 0; y < screen.height; ++y) {
      for (var x = 0; x < screen.width; ++x) {
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
    final VectorMath.V coordinate;
    final VectorMath.V direction;
    final int cell;
    final double distance;

    Hit(VectorMath.V coordinate, VectorMath.V direction, int cell, double distance) {
      this.coordinate = coordinate;
      this.direction = direction;
      this.cell = cell;
      this.distance = distance;
    }
  }

  private void castAllColumns(List<Hit> hits) {
    final var playerToPlane = VectorMath.times(input.player.dir, input.camera.distance);
    final var planeDir = VectorMath.times(VectorMath.rot(VectorMath.dtor(90.0)), input.player.dir);
    final var planeStart = VectorMath.vplus(
        input.player.pos, VectorMath.vplus(playerToPlane, VectorMath.times(planeDir, input.camera.width / 2.0)));

    for (var x = 0; x < screen.width; ++x) {
      var rayOnPlane =
          VectorMath.vplus(planeStart,
              VectorMath.times(VectorMath.times(VectorMath.rot(VectorMath.dtor(180.0)), planeDir),
                  input.camera.width * (1.0 - (double) x / screen.width)));
      var rayDir = VectorMath.vminus(rayOnPlane, input.player.pos);

      var hit = false;
      for (var distance = 0.01; distance < 100. && !hit; distance += 0.01) {
        VectorMath.V check = VectorMath.vplus(input.player.pos, VectorMath.times(rayDir, distance));
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

  private int rayCast(int x, int y, List<Hit> hits, Screen screen) {
    final var hit = hits.get(x);
    if (hit == null) {
      return drawCeiling();
    }

    final var distance = hit.distance;
    final var wallheight = Math.min(1.0 / distance, screen.height);

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

  private int drawHit(Hit hit, double y, double height) {
    final var u = Math.abs((hit.coordinate.x + hit.coordinate.y) % 1.0);
    final var v = VectorMath.clamp(y / height, 0., 0.999);
    final var asset = level.assets.get(hit.cell - 1); // assets start from 1 on the map

    final var tx = (asset.getWidth() * u);
    final var ty = (asset.getHeight() * v);

    return asset.getPixels()[(int)tx + (int)(ty * asset.getWidth())];
  }

  private int drawCeiling() {
    return 0x0000FF;
  }

  private int drawFloor() {
    return 0x00FF00;
  }
}
