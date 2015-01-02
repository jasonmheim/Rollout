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

/**
 * Combination of {@link Station} and a distance. Does not include the origin point from which the
 * distance was calculated.
 */
public class StationDistance implements Comparable<StationDistance> {

  private final Station station;
  private final String direction;

  protected final double milesToLocation;

  public StationDistance(Station station, double milesToLocation, String direction) {
    this.station = station;
    this.milesToLocation = milesToLocation;
    this.direction = direction;
  }

  public double getMilesToLocation() {
    return milesToLocation;
  }

  public double getFeetToLocation() {
    return milesToLocation * 5280;
  }

  public double getKilometersToLocation() {
    return milesToKilometers(milesToLocation);
  }

  public double getMetersToLocation() {
    return milesToMeters(milesToLocation);
  }

  public Station getStation() {
    return station;
  }

  public String getDistanceString() {
    if (milesToLocation < 0.1) {
      return String.valueOf((int) getFeetToLocation()) + " Feet " + direction;
    }
    return String.format("%.2f Miles ", milesToLocation) + direction;
  }

  @Override
  public int compareTo(StationDistance other) {
    return Double.compare(milesToLocation, other.milesToLocation);
  }

  @Override
  public String toString() {
    return station.stationName + ": " + getDistanceString();
  }

  protected static double milesToFeet(double miles) {
    return miles * 5280;
  }

  protected static double milesToKilometers(double miles) {
    return miles * 1.609344;
  }

  protected static double milesToMeters(double miles) {
    return miles * 1609.344;
  }
}
