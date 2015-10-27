package com.michaldrabik.compassnav;

import java.util.List;

public class Utils {

  /**
   * Transforms 'True North' degrees value into 0 to 360 degrees.
   *
   * @param value True North (from -180 to 180 degrees)
   * @return 0 to 360 degrees
   */
  public static double normalizeDegree(double value) {
    return (value + 360) % 360;
  }

  /**
   * Returns average value of list of floats.
   *
   * @param list Input list.
   * @return Average value.
   */
  public static float average(List<Float> list) {
    if (list.isEmpty()) {
      return 0;
    }
    Float sum = 0f;
    for (Float f : list) {
      sum += f;
    }
    return sum / (float) list.size();
  }

  /**
   * Clamps given float value.
   * @param value input.
   * @param min minimal value.
   * @param max maximal value.
   * @return clamped value.
   */
  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
