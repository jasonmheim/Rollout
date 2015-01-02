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

import android.database.AbstractCursor;

/**
 * A custom cursor that always has exactly one row.
 */
public class StationDataCursor extends AbstractCursor {

  private static final String[] COLUMN_NAMES = {
      "StationDataJson"
  };

  // Default to an empty list inside the station list object
  private String stationDataJson = "{\"stationBeanList\":[]}";

  /** Visible only to the content provider in our package */
  void setStationDataJson(String stationDataJson) {
    this.stationDataJson = stationDataJson;
    super.onChange(false);
  }

  @Override
  public int getCount() {
    return 1;
  }

  @Override
  public String[] getColumnNames() {
    return COLUMN_NAMES;
  }

  @Override
  public String getString(int i) {
    if (i == 0) {
      return stationDataJson;
    }
    throw new IndexOutOfBoundsException("Unsupported StationDataCursor index: " + i);
  }

  @Override
  public short getShort(int i) {
    throw new UnsupportedOperationException("Only getString is supported.");
  }

  @Override
  public int getInt(int i) {
    throw new UnsupportedOperationException("Only getString is supported.");
  }

  @Override
  public long getLong(int i) {
    throw new UnsupportedOperationException("Only getString is supported.");
  }

  @Override
  public float getFloat(int i) {
    throw new UnsupportedOperationException("Only getString is supported.");
  }

  @Override
  public double getDouble(int i) {
    throw new UnsupportedOperationException("Only getString is supported.");
  }

  @Override
  public boolean isNull(int i) {
    throw new UnsupportedOperationException("Only getString is supported.");
  }
}
