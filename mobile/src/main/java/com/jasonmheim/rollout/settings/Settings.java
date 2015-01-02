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

package com.jasonmheim.rollout.settings;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.jasonmheim.rollout.Constants;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.jasonmheim.rollout.Constants.DISCLAIMER_VERSION;
import static com.jasonmheim.rollout.Constants.PREF_AGREED_DISCLAIMER_VERSION;
import static com.jasonmheim.rollout.Constants.PREF_DESTINATION_HOME_LATITUDE;
import static com.jasonmheim.rollout.Constants.PREF_DESTINATION_HOME_LONGITUDE;
import static com.jasonmheim.rollout.Constants.PREF_DESTINATION_HOME_SET;
import static com.jasonmheim.rollout.Constants.PREF_DESTINATION_WORK_LATITUDE;
import static com.jasonmheim.rollout.Constants.PREF_DESTINATION_WORK_LONGITUDE;
import static com.jasonmheim.rollout.Constants.PREF_DESTINATION_WORK_SET;
import static com.jasonmheim.rollout.Constants.PREF_ENABLE_VIBRATION;

/**
 * Convenience class for accessing the user's current settings.
 */
@Singleton
public class Settings {

  private final SharedPreferences sharedPreferences;

  @Inject
  Settings(SharedPreferences sharedPreferences) {
    this.sharedPreferences = sharedPreferences;
  }

  public boolean isVibrationEnabled() {
    return sharedPreferences.getBoolean(PREF_ENABLE_VIBRATION, false);
  }

  public boolean isHomeDestinationActive() {
    return sharedPreferences.getBoolean(PREF_DESTINATION_HOME_SET, false);
  }

  public Location getHomeDestination() {
    String latitudeString = sharedPreferences.getString(PREF_DESTINATION_HOME_LATITUDE, "");
    String longitudeString = sharedPreferences.getString(PREF_DESTINATION_HOME_LONGITUDE, "");
    return parseLocation(latitudeString, longitudeString);
  }

  public boolean isWorkDestinationActive() {
    return sharedPreferences.getBoolean(PREF_DESTINATION_WORK_SET, false);
  }

  public Location getWorkDestination() {
    String latitudeString = sharedPreferences.getString(PREF_DESTINATION_WORK_LATITUDE, "");
    String longitudeString = sharedPreferences.getString(PREF_DESTINATION_WORK_LONGITUDE, "");
    return parseLocation(latitudeString, longitudeString);
  }

  public int getFullThreshold() {
    return getThresholdValue(Constants.PREF_FULL_THRESHOLD);
  }

  public int getEmptyThreshold() {
    return getThresholdValue(Constants.PREF_EMPTY_THRESHOLD);
  }

  public boolean isDisclaimerAgreed() {
    return sharedPreferences.getInt(PREF_AGREED_DISCLAIMER_VERSION, -1) >= DISCLAIMER_VERSION;
  }

  private int getThresholdValue(String key) {
    String valueString = sharedPreferences.getString(key, "0");
    try {
      return Integer.parseInt(valueString);
    } catch (RuntimeException ex) {
      Log.w("Rollout", "Failed to parse threshold key " + key + " value " + valueString);
    }
    return 0;
  }

  private Location parseLocation(String latitudeString, String longitudeString) {
    try {
      Location result = new Location("");
      result.setLatitude(Double.parseDouble(latitudeString));
      result.setLongitude(Double.parseDouble(longitudeString));
      return result;
    } catch (RuntimeException ex) {
      Log.w("Rollout", "Home location extraction failed", ex);
    }
    return null;
  }
}
