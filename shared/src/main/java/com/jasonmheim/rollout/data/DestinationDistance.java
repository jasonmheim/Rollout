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
 * A variant on StationDistance used for calculating while riding. This takes into account both the
 * distance from current location and the distance from destination. When comparing distance, the
 * distance from the current location to the station is not weighed as heavily as the distance from
 * the station to the destination. The resulting sort will favor stations closest to the destination
 * but also consider the proximity to the current location when making decisions.
 * <p>
 * This calculation makes a crude assumption that it will take the user four times as long to cover
 * the same distance by foot than by bike.
 */
public class DestinationDistance extends StationDistance {

  private final double milesToDestination;

  public DestinationDistance(
      Station station,
      double milesToDestination,
      double milesToLocation,
      String direction) {
    super(station, milesToLocation, direction);
    this.milesToDestination = milesToDestination;
  }

  public double getMilesToDestination() {
    return milesToDestination;
  }

  public double getFeetToDestination() {
    return milesToFeet(milesToDestination);
  }

  public double getMetersToDestination() {
    return milesToMeters(milesToDestination);
  }

  public double getKilometersToDestination() {
    return milesToKilometers(milesToDestination);
  }

  @Override
  public int compareTo(StationDistance otherType) {
    if (otherType instanceof DestinationDistance) {
      DestinationDistance other = (DestinationDistance) otherType;
      return Double.compare(
          (milesToDestination * 4) + milesToLocation,
          (other.milesToDestination * 4) + other.milesToLocation);
    }
    return super.compareTo(otherType);
  }
}
