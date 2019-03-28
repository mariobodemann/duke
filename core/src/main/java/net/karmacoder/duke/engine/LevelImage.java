package net.karmacoder.duke.engine;

import net.karmacoder.duke.engine.RayCasting.Level;
import net.karmacoder.duke.image.Image;

public class LevelImage implements Image {
  private final Level level;

  public LevelImage(Level level) {
    this.level = level;
  }

  @Override
  public int[] getPixels() {
    final var cellcount = level.width * level.height;
    final var arr = new int[cellcount];
    for (var i = 0; i < cellcount; ++i) {
      if (level.cells.get(i) != 0) {
        final var asset = level.assets.get(level.cells.get(i) - 1);
        final var centerIndex = (int) (asset.getWidth() / 2.) + (int) (asset.getHeight() / 2. * asset.getWidth());
        arr[i] = asset.getPixels()[centerIndex];
      } else {
        arr[i] = 0;
      }
    }
    return arr;
  }

  @Override
  public int getWidth() {
    return level.width;
  }

  @Override
  public int getHeight() {
    return level.height;
  }
}
