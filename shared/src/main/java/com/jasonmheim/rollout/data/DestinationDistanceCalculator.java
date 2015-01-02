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
public class DestinationDistanceCalculator implements Function<Station, DestinationDistance> {

  private final double latitudeRadians;
  private final double longitudeRadians;
  private final double destinationLatitudeRadians;
  private final double destinationLongitudeRadians;

  public DestinationDistanceCalculator(
      double latitude,
      double longitude,
      double destinationLatitude,
      double destinationLongitude) {
    this.latitudeRadians = toRadians(latitude);
    this.longitudeRadians = toRadians(longitude);
    this.destinationLatitudeRadians = toRadians(destinationLatitude);
    this.destinationLongitudeRadians = toRadians(destinationLongitude);
  }

  @Override
  public DestinationDistance apply(Station station) {
    double stationLatitudeRadians = toRadians(station.latitude);
    double stationLongitudeRadians = toRadians(station.longitude);
    return new DestinationDistance(
        station,
        GeoUtils.distanceInMiles(destinationLatitudeRadians, destinationLongitudeRadians,
            stationLatitudeRadians, stationLongitudeRadians),
        GeoUtils.distanceInMiles(latitudeRadians, longitudeRadians,
            stationLatitudeRadians, stationLongitudeRadians),
        GeoUtils.compassDirection(latitudeRadians, longitudeRadians,
            stationLatitudeRadians, stationLongitudeRadians));
  }
}
