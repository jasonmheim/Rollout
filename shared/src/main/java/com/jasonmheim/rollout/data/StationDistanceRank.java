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
 * Combines a {@link StationDistance} and a "rank" value that represents its overall ranking in a
 * sorted list. When station data are processed, this can be used to report a result that may not
 * rank first when ordered by distance, but was the first with available docks or bikes. A rank
 * greater than zero indicates the number of stations that were skipped to arrive at this result.
 */
public class StationDistanceRank {

  private final StationDistance stationDistance;
  private final int rank;
  private final int limitedRank;

  public StationDistanceRank(StationDistance stationDistance, int rank) {
    this.stationDistance = stationDistance;
    this.rank = rank;
    this.limitedRank = Math.min(3, rank);
  }

  public int getRank() {
    return rank;
  }

  /**
   * Returns the rank, or 3, whichever is less. This is a helper method for functionality that
   * indicates a level of warning between 1 and 3 to indicate that this station is not ranked
   * first, capping at level 3 so as to avoid a combinatorial explosion of icons.
   */
  public int getLimitedRank() {
    return limitedRank;
  }

  public StationDistance getStationDistance() {
    return stationDistance;
  }
}
