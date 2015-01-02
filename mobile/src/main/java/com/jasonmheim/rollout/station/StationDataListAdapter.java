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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.data.StationDistance;

import java.util.List;

/**
 * Converts a {@link StationDistance} to a view as a list item.
 */
public class StationDataListAdapter extends ArrayAdapter<StationDistance> {

  private final ActionManager actionManager;

  public StationDataListAdapter(
      Context context,
      int resource,
      List<StationDistance> objects,
      ActionManager actionManager) {
    super(context, resource, objects);
    this.actionManager = actionManager;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    StationDataRowViewHolder viewHolder;
    if (convertView == null) {
      LayoutInflater mInflater =
          (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
      convertView = mInflater.inflate(R.layout.stationdistance_list_item, null);
      viewHolder = new StationDataRowViewHolder(convertView);
    } else {
      viewHolder = (StationDataRowViewHolder) convertView.getTag();
    }
    if (actionManager.getAction() == Constants.ACTION_RIDE) {
      viewHolder.setForRiding(getItem(position));
    } else {
      viewHolder.setForNotRiding(getItem(position));
    }
    return convertView;
  }

}
