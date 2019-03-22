package net.karmacoder.duke.math;

import static java.lang.Math.*;

public class VectorMath {

  public static class V {
    public double x;
    public double y;

    public V(double x, double y) {
      this.x = x;
      this.y = y;
    }
  }

  public static class Hsv {
    public double h;
    public double s;
    public double v;
  }

  public static class M {
    public double[] m = new double[4];

    public M(double a, double b, double c, double d) {
      m[0] = a;
      m[1] = b;
      m[2] = c;
      m[3] = d;
    }

    public double a() {
      return m[0];
    }

    public double b() {
      return m[1];
    }

    public double c() {
      return m[2];
    }

    public double d() {
      return m[3];
    }
  }

  public static M rot(double angle) {
    double c = cos(angle);
    double s = sin(angle);

    return new M(c, -s, s, c);
  }

  public static V times(M m, V v) {
    return new V(m.a() * v.x + m.b() * v.y, m.c() * v.x + m.d() * v.y);
  }

  public static V vplus(V v1, V v2) {
    return new V(v1.x + v2.x, v1.y + v2.y);
  }

  public static V vminus(V v1, V v2) {
    return new V(v1.x - v2.x, v1.y - v2.y);
  }

  public static V times(V v, double s) {
    return new V(v.x * s, v.y * s);
  }

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static V normalize(V v) {
    double l = sqrt(v.x * v.x + v.y * v.y);
    return times(v, 1.0 / l);
  }

  public static double dtor(double angle) {
    return (PI / 180.0) * angle;
  }

  public static Hsv rgbToHsv(int rgb) {
    var r = (rgb >> 16) & 0xff;
    var g = (rgb >> 8) & 0xff;
    var b = rgb & 0xff;

    Hsv hsv = new Hsv();

    int rgbMin, rgbMax;

    rgbMin = r < g ? (r < b ? r : b) : (g < b ? g : b);
    rgbMax = r > g ? (r > b ? r : b) : (g > b ? g : b);

    hsv.v = rgbMax;
    if (hsv.v == 0) {
      hsv.h = 0;
      hsv.s = 0;
      return hsv;
    }

    hsv.s = 255 * ((long) (rgbMax - rgbMin)) / hsv.v;
    if (hsv.s == 0) {
      hsv.h = 0;
      return hsv;
    }

    if (rgbMax == r) {
      hsv.h = 43. * (g - b) / (rgbMax - rgbMin);
    } else if (rgbMax == g) {
      hsv.h = 85. + 43. * (b - r) / (rgbMax - rgbMin);
    } else {
      hsv.h = 171. + 43. * (r - g) / (rgbMax - rgbMin);
    }

    return hsv;
  }

  public static int hsvToRgb(Hsv hsv) {
    double c = hsv.v * hsv.s;
    double x = c * (1.0 - abs((hsv.h / 60.0 % 2.0) - 1.0));
    double m = hsv.v - c;

    double r = 0.0;
    double g = 0.0;
    double b = 0.0;

    if (0 <= hsv.h && hsv.h < 60) {
      r = c;
      g = x;
      b = 0;
    } else if (60 <= hsv.h && hsv.h < 120) {
      r = x;
      g = c;
      b = 0;
    } else if (120 <= hsv.h && hsv.h < 180) {
      r = 0;
      g = c;
      b = x;
    } else if (180 <= hsv.h && hsv.h < 240) {
      r = 0;
      g = x;
      b = c;
    } else if (240 <= hsv.h && hsv.h < 300) {
      r = x;
      g = 0;
      b = c;
    } else if (300 <= hsv.h && hsv.h < 360) {
      r = c;
      g = 0;
      b = x;
    }

    r = r + m;
    g = g + m;
    b = b + m;

    return (((int) r & 0xFF) << 16) + (((int) g & 0xFF) << 8) + ((int) b & 0xFF);
  }
}
