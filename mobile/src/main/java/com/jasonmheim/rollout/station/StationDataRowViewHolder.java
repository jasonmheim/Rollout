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

import android.text.format.DateUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.data.Station;
import com.jasonmheim.rollout.data.StationDistance;
import com.jasonmheim.rollout.data.StationList;

import java.text.DateFormat;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Holder for the various widgets within a row view of station data. Classes like this are an
 * optimization for list views. Without this class, then each time a particular row view was
 * recycled the system would have to find all the inner view objects again. This class pre-extracts
 * the references to the views so that they can be immediately referenced when the object is
 * recycled for a different {@link StationDistance} object.
*/
class StationDataRowViewHolder {
  private final TextView textViewTL;
  private final TextView textViewTR;
  private final TextView textViewBL;
  private final TextView textViewBR;

  private final TextView brokenBikes;
  private final TextView availableBikes;
  private final TextView availableDocks;

  public StationDataRowViewHolder(View convertView) {
    textViewTL = (TextView) convertView.findViewById(R.id.textViewTL);
    textViewTR = (TextView) convertView.findViewById(R.id.textViewTR);
    textViewBL = (TextView) convertView.findViewById(R.id.textViewBL);
    textViewBR = (TextView) convertView.findViewById(R.id.textViewBR);

    brokenBikes = (TextView) convertView.findViewById(R.id.brokenBikes);
    availableBikes = (TextView) convertView.findViewById(R.id.availableBikes);
    availableDocks = (TextView) convertView.findViewById(R.id.availableDocks);
    convertView.setTag(this);
  }

  /**
   * Sets the row data for when the user is riding.
   */
  public void setForRiding(StationDistance stationDistance) {
    Station station = stationDistance.getStation();
    textViewTL.setText(station.stationName);
    // TODO: Use string resources and proper pluralization
    textViewTR.setText("Docks:\t" + station.availableDocks);
    textViewBL.setText(stationDistance.getDistanceString());
    textViewBR.setText("");

    setBackground(station);
  }

  /**
   * Sets the row data for when the user is not riding.
   */
  public void setForNotRiding(StationDistance stationDistance) {
    Station station = stationDistance.getStation();
    textViewTL.setText(station.stationName);
    // TODO: Use string resources and proper pluralization
    textViewTR.setText("Bikes:\t" + station.availableBikes);
    textViewBL.setText(stationDistance.getDistanceString());
    int duds = station.totalDocks - (station.availableBikes + station.availableDocks);
    textViewBR.setText("Duds:\t" + duds);

    setBackground(station);
  }

  private void setBackground(Station station) {
    float availableBikesWeight = (float) station.availableBikes / (float) station.totalDocks;
    float availableDocksWeight = (float) station.availableDocks / (float) station.totalDocks;
    float brokenBikesWeight = 1f - (availableBikesWeight + availableDocksWeight);

    brokenBikes.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT, brokenBikesWeight));
    availableBikes.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT, availableBikesWeight));
    availableDocks.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT, availableDocksWeight));
  }

  /**
   * Special method for showing data about the entire list. This is not actually part of the list
   * view, this is for an extra row that sits above the full list.
   */
  public void setTopRowText(StationList stationList, String action) {
    textViewTL.setText("Last sync at: " + DateUtils.formatSameDayTime(
        stationList.timestamp,
        System.currentTimeMillis(),
        DateFormat.SHORT,
        DateFormat.SHORT));
    textViewBL.setText(action);
    brokenBikes.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT, 1f));
    availableBikes.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT, 0f));
    availableDocks.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT, 0f));
  }
}
