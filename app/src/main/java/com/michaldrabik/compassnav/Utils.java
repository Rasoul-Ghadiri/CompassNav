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
}
