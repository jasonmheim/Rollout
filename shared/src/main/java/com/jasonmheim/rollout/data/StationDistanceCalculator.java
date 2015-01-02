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

import com.google.common.base.Function;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Given a current location, converts instance of {@link Station} to {@link StationDistance}.
 * Distance is calculated in miles directly from point a to b, not in "Manhattan distance".
 * The given latitude and longitude are expected to be decimal degrees.
 */
public class StationDistanceCalculator implements Function<Station, StationDistance> {

  private final double latitude;
  private final double longitude;
  private final double latitudeRadians;
  private final double longitudeRadians;

  public StationDistanceCalculator(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.latitudeRadians = toRadians(latitude);
    this.longitudeRadians = toRadians(longitude);
  }

  @Override
  public StationDistance apply(Station station) {
    double stationLatitudeRadians = toRadians(station.latitude);
    double stationLongitudeRadians = toRadians(station.longitude);
    return new StationDistance(
        station,
        GeoUtils.distanceInMiles(latitudeRadians, longitudeRadians,
            stationLatitudeRadians, stationLongitudeRadians),
        GeoUtils.compassDirection(latitudeRadians, longitudeRadians,
            stationLatitudeRadians, stationLongitudeRadians));
  }
}
