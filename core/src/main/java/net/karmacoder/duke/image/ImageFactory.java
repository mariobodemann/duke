package net.karmacoder.duke.image;

public interface ImageFactory {
  Image create(String source) throws Exception;

  Image create(String source, float scale) throws Exception;

  Image create(String source, int width, int height) throws Exception;
}
