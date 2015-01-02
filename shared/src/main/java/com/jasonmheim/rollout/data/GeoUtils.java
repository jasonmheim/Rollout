/*
 * Copyright (C) 2014 Jason M. Heim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jasonmheim.rollout.data;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;

/**
 * Utility methods for calculating geographic distance and direction between points
 */
class GeoUtils {

  // 16 point compass where N is at index 0, counting up clockwise from there.
  private static final String[] DIRECTIONS = {
      "N",
      "NNE",
      "NE",
      "ENE",
      "E",
      "ESE",
      "SE",
      "SSE",
      "S",
      "SSW",
      "SW",
      "WSW",
      "W",
      "WNW",
      "NW",
      "NNW",
  };

  static double distanceInMiles(double lat1, double lon1, double lat2, double lon2) {
    double dist = acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1));
    dist = toDegrees(dist);
    dist = dist * 60 * 1.1515;
    return (dist);
  }

  static String compassDirection(double lat1, double lon1, double lat2, double lon2) {
    double theta = lon2 - lon1;
    double dy = sin(theta) * cos(lat2);
    double dx = (cos(lat1) * sin(lat2)) - (sin(lat1) * cos(lat2) * cos(theta));

    // Get the arctangent in radians, measuring clockwise from true north.
    // Normalize to a range from -8 to 8, which represents 16 tick marks on the compass
    // Add 16.5 - this is one full rotation plus half a tick mark so that we can round down
    // Compute modulo 16 to ensure range 0 to 16.
    // Round down by casting to an int to get a compass point index.
    int compassPoint = (int) (((atan2(dy, dx) * 8 / PI) + 16.5) % 16);
    return DIRECTIONS[compassPoint];
  }

}
