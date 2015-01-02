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

package com.jasonmheim.rollout.sync;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.data.StationList;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Helper class to manage the last known station list. This downloads the data from the content
 * provider but also registers a {@link ContentObserver} so that any updates to data, location, or
 * action will trigger the data to be re-obtained and broadcast within the process.
 */
@Singleton
public class LastKnownStationList {

  private final Bus bus;
  private final Gson gson;
  private final ContentResolver contentResolver;

  private StationList stationList;

  @Inject
  public LastKnownStationList(
      Bus bus,
      ContentResolver contentResolver,
      Gson gson) {
    this.bus = bus;
    this.contentResolver = contentResolver;
    this.gson = gson;
  }

  public synchronized StationList get() {
    if (stationList == null) {
      Cursor cursor = contentResolver.query(Constants.STATION_URI, null, null, null, null);
      try {
        // TODO: make a helper for serialization/deserialization
        stationList = gson.fromJson(cursor.getString(0), StationList.class);
        contentResolver.registerContentObserver(Constants.STATION_URI, true, new Observer());
        Log.i("Rollout", "Initialized last know station list.");
      } catch (RuntimeException ex) {
        Log.w("Rollout", "Failed to deserialize station list during initialization.", ex);
      } finally {
        cursor.close();
      }
    }
    return stationList;
  }

  /**
   * Observer for changes to station list data.
   */
  private class Observer extends ContentObserver {

    // TODO: just make the outer class the content observer?
    public Observer() {
      super(new Handler());
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
      super.onChange(selfChange, uri);
      Cursor cursor = contentResolver.query(Constants.STATION_URI, null, null, null, null);
      try {
        stationList = gson.fromJson(cursor.getString(0), StationList.class);
        Log.i("Rollout", "Content observer received update.");
        bus.post(new StationDataUpdateEvent(stationList));
      } catch (RuntimeException ex) {
        Log.w("Rollout", "Failed to deserialize station list from update.", ex);
      } finally {
        cursor.close();
      }
    }
  }
}
