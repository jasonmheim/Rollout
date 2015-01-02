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

package com.jasonmheim.rollout.station;

import android.location.Location;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.data.DestinationDistanceCalculator;
import com.jasonmheim.rollout.data.Station;
import com.jasonmheim.rollout.data.StationDistance;
import com.jasonmheim.rollout.data.StationDistanceCalculator;
import com.jasonmheim.rollout.data.StationDistanceRank;
import com.jasonmheim.rollout.data.StationList;
import com.jasonmheim.rollout.location.LocationManager;
import com.jasonmheim.rollout.settings.Settings;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.jasonmheim.rollout.Constants.ACTION_RIDE;

/**
 * Station data utility class, handles sorting and threshold data based on the current action,
 * location, and other settings.
 */
@Singleton
public class StationDataProcessor {

  private final ActionManager actionManager;
  private final LocationManager locationManager;
  private final Settings settings;

  @Inject
  public StationDataProcessor(
      ActionManager actionManager,
      LocationManager locationManager,
      Settings settings) {
    this.actionManager = actionManager;
    this.locationManager = locationManager;
    this.settings = settings;
  }

  /**
   * Converts the given {@code stationList} to a list of {@link com.jasonmheim.rollout.data.StationDistance} instances which
   * are sorted by distance. If the current action is riding, and a destination is specified, then
   * these are sorted by proximity to the destination, with additional weight given to the user's
   * current location. Otherwise the results are sorted by proximity to the user's current location.
   */
  public List<StationDistance> orderClosestStations(StationList stationList) {
    if (stationList == null) {
      return Collections.emptyList();
    }
    return orderClosestStations(stationList, actionManager.getAction());
  }

  /**
   * Takes the given {@code stationList} and searches for the station that best suits the user's
   * current needs. The stations are ordered as described in {@link #orderClosestStations}. If the
   * user is currently riding, the first station with available docks is returned. Otherwise the
   * first station with available bikes is returned.
   */
  public StationDistanceRank getClosestAvailableStation(StationList stationList) {
    int action = actionManager.getAction();
    List<StationDistance> orderedStations = orderClosestStations(stationList, action);
    if (action == ACTION_RIDE) {
      return getClosestStationWithDocks(orderedStations, settings.getFullThreshold());
    }
    return getClosestStationWithBikes(orderedStations, settings.getEmptyThreshold());
  }

  private StationDistanceRank getClosestStationWithDocks(
      List<StationDistance> orderedStations, int docks) {
    int rank = 0;
    for (StationDistance stationDistance : orderedStations) {
      if (stationDistance.getStation().availableDocks > docks) {
        return new StationDistanceRank(stationDistance, rank);
      }
      rank++;
    }
    return null;
  }

  private StationDistanceRank getClosestStationWithBikes(
      List<StationDistance> orderedStations, int bikes) {
    int rank = 0;
    for (StationDistance stationDistance : orderedStations) {
      if (stationDistance.getStation().availableBikes > bikes) {
        return new StationDistanceRank(stationDistance, rank);
      }
      rank++;
    }
    return null;
  }

  private List<StationDistance> orderClosestStations(StationList stationList, int action) {
    if (stationList == null) {
      return Collections.emptyList();
    }
    Location location = locationManager.getLastLocation();
    if (location != null) {
      Location destination = actionManager.getDestination();
      Function<Station, ? extends StationDistance> calculator;
      if (action == ACTION_RIDE && destination != null) {
        calculator = new DestinationDistanceCalculator(
            location.getLatitude(), location.getLongitude(),
            destination.getLatitude(), destination.getLongitude());
      } else {
        calculator = new StationDistanceCalculator(location.getLatitude(), location.getLongitude());
      }
      return Ordering.natural().immutableSortedCopy(
          Iterables.transform(stationList.stationBeanList, calculator));
    }
    return Collections.emptyList();
  }
}
