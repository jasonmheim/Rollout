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

package com.jasonmheim.rollout;

import android.accounts.Account;
import android.net.Uri;

import junit.framework.Assert;

import java.net.MalformedURLException;
import java.net.URL;

import static android.content.Context.MODE_MULTI_PROCESS;
import static android.content.Context.MODE_PRIVATE;

/**
 * Common constants for the Rollout application.
 */
public final class Constants {

  public static final String AUTHORITY = "com.jasonmheim.rollout";
  public static final Account ACCOUNT = new Account("BikeShareData", AUTHORITY);
  public static final Uri AUTHORITY_URI = new Uri.Builder()
      .authority(AUTHORITY)
      .scheme("content")
      .build();

  public static final Uri STATION_URI = AUTHORITY_URI.buildUpon().path("station").build();
  public static final Uri ACTION_URI = AUTHORITY_URI.buildUpon().path("action").build();
  public static final Uri LOCATION_URI = AUTHORITY_URI.buildUpon().path("location").build();
  public static final Uri SETTINGS_URI = AUTHORITY_URI.buildUpon().path("settings").build();
  /** Use this {@link Uri} to trigger a content observer when anything is updated */
  public static final Uri ANY_URI = AUTHORITY_URI.buildUpon().path("any").build();

  public static final int ACTION_IDLE = 0;
  public static final int ACTION_SEARCH = 1;
  public static final int ACTION_RIDE = 2;
  public static final int ACTION_SILENCE = 3;

  public static final String DESTINATION_NAME_HOME = "Home";
  public static final String DESTINATION_NAME_WORK = "Work";

  // Shared preferences key and mode. Preferences are used across processes.
  public static final String PREF_KEY = "Rollout";
  public static final int PREF_MODE = MODE_PRIVATE & MODE_MULTI_PROCESS;

  // NB: These values must match those in settings.xml
  public static final String PREF_ENABLE_VIBRATION = "pref_enable_vibration";
  public static final String PREF_EMPTY_THRESHOLD = "pref_empty_threshold";
  public static final String PREF_FULL_THRESHOLD = "pref_full_threshold";
  public static final String PREF_DESTINATION_HOME_SET = "pref_destination_home_set";
  public static final String PREF_DESTINATION_HOME_LATITUDE = "pref_destination_home_latitude";
  public static final String PREF_DESTINATION_HOME_LONGITUDE = "pref_destination_home_longitude";
  public static final String PREF_DESTINATION_WORK_SET = "pref_destination_work_set";
  public static final String PREF_DESTINATION_WORK_LATITUDE = "pref_destination_work_latitude";
  public static final String PREF_DESTINATION_WORK_LONGITUDE = "pref_destination_work_longitude";

  /**
   * This preference is not used in the settings dialog - it is set when users agree to use the
   * application at their own risk.
   */
  public static final String PREF_AGREED_DISCLAIMER_VERSION = "pref_agreed_disclaimer_version";

  // TODO: these should be renamed for being used as query parameters after the URIs above are
  // being used for updating the content provider.
  public static final String UPDATE_KEY_LOCATION = "location";
  public static final String UPDATE_KEY_ACTION = "action";
  public static final String UPDATE_KEY_DESTINATION = "dest";

  public static final URL STATION_DATA_URL;

  public static final int DISCLAIMER_VERSION = 1;

  static {
    try {
      STATION_DATA_URL = new URL("http://www.citibikenyc.com/stations/json");
    } catch(MalformedURLException ex) {
      // Unreachable
      throw new AssertionError(ex);
    }
  }

  public static final long SECONDS_PER_MINUTE = 60;
  public static final long MS_PER_SECOND = 1000;
  public static final long MS_PER_MINUTE = SECONDS_PER_MINUTE * MS_PER_SECOND;

  // Do not instantiate
  private Constants() {}
}
