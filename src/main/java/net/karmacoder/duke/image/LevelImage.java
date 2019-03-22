package net.karmacoder.duke.image;

import static net.karmacoder.duke.engine.RayCasting.Level;

public class LevelImage implements Image {
  private final Level level;

  public LevelImage(Level level) {
    this.level = level;
  }

  @Override
  public int[] getPixels() {
    final var cellcount = level.width * level.height;
    final int[] arr = new int[cellcount];
    for (int i = 0; i < cellcount; ++i) {
      if (level.cells.get(i) != 0) {
        final Image asset = level.assets.get(level.cells.get(i) - 1);
        final int centerIndex = (int) (asset.getWidth() / 2.) + (int) (asset.getHeight() / 2. * asset.getWidth());
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
