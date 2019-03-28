package net.karmacoder.duke.engine;

import net.karmacoder.duke.image.Image;

interface Engine<Level, Input> {
  boolean loadLevel(Level level);

  boolean processInput(Input input);

  Image renderFrame();
}
