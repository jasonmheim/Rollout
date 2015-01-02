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

package com.jasonmheim.rollout.action;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.RemoteException;
import android.util.Log;

import com.google.common.base.Objects;
import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.settings.Settings;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages the current action that the user is doing.
 */
@Singleton
public class ActionManager {

  static final String ACTION_KEY = "ActionManagerKey";
  static final String DESTINATION_KEY = "ActionManagerDestinationKey";

  private final ContentResolver contentResolver;
  private final Settings preferences;
  private final SharedPreferences sharedPreferences;

  @Inject
  ActionManager(
      ContentResolver contentResolver,
      Settings preferences,
      SharedPreferences sharedPreferences) {
    this.contentResolver = contentResolver;
    this.preferences = preferences;
    this.sharedPreferences = sharedPreferences;
  }

  /**
   * Equivalent to {@link #setAction(int, String)} with a null {@code destinationName}.
   */
  public void setAction(int action) {
    this.setAction(action, null);
  }

  /**
   * Sets the current {@code action} such as {@link Constants#ACTION_IDLE} as well as an optional
   * {@code destinationName} such as {@link Constants#DESTINATION_NAME_HOME}. Note that setting the
   * destination name only has an effect if the action is {@link Constants#ACTION_RIDE}; otherwise
   * it is ignored.
   * <p>
   * Setting the action may have a number of side effects. The priority of notifications may change,
   * as well as their content depending on what the user is doing. The frequency that station data
   * and current location will be updated may also change.
   */
  public void setAction(int action, String destinationName) {
    int oldAction = sharedPreferences.getInt(ACTION_KEY, -1);
    String oldDestinationName = sharedPreferences.getString(DESTINATION_KEY, null);
    if (oldAction != action || !Objects.equal(oldDestinationName, destinationName)) {
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putInt(ACTION_KEY, action);
      if (destinationName == null) {
        editor.remove(DESTINATION_KEY);
      } else {
        editor.putString(DESTINATION_KEY, destinationName);
      }
      editor.apply();
      onActionChanged();
    }
  }

  public int getAction() {
    return sharedPreferences.getInt(ACTION_KEY, Constants.ACTION_IDLE);
  }

  public String getDestinationName() {
    return sharedPreferences.getString(DESTINATION_KEY, null);
  }

  /**
   * If there is a current destination, this attempts to ascertain its {@link Location}. If the
   * destination is not set, or the location cold not be computed, this returns {@code null}.
   */
  public Location getDestination() {
    String destinationName = getDestinationName();
    if (destinationName == null) {
      return null;
    } else if (destinationName.equals(Constants.DESTINATION_NAME_HOME)) {
      return preferences.getHomeDestination();
    } else if (destinationName.equals(Constants.DESTINATION_NAME_WORK)) {
      return preferences.getWorkDestination();
    }
    return null;
  }

  /**
   * Returns a human readable description of the current action, which may also include destination
   * information.
   */
  public String getActionDisplayName() {
    // TODO: Extract string resources with formatting and such. Will need getString() dependency
    switch (getAction()) {
      case Constants.ACTION_SEARCH:
        return "Searching for a bike";
      case Constants.ACTION_SILENCE:
        return "Silenced";
      case Constants.ACTION_IDLE:
        return "Idle";
      case Constants.ACTION_RIDE:
        if (sharedPreferences.contains(DESTINATION_KEY)) {
          return "Riding to " + sharedPreferences.getString(DESTINATION_KEY, "Unknown");
        }
        return "Roaming on a bike";
    }
    return "Current action unknown";
  }

  /**
   * This method should only be invoked at application startup; it ensures that the core content
   * provider is bootstrapped with the current action and that station data sync is activated.
   */
  public void initialize() {
    ContentResolver.setSyncAutomatically(Constants.ACCOUNT, Constants.AUTHORITY, true);
    ContentResolver.setIsSyncable(Constants.ACCOUNT, Constants.AUTHORITY, 1);
    // TODO: remember why I thought this was necessary and document it :-P
    onActionChanged();
  }

  private void onActionChanged() {
    // TODO: Switch this to use the ACTION_URI; drop the content values.
    ContentValues contentValues = new ContentValues();
    contentValues.put(Constants.UPDATE_KEY_ACTION, true);
    ContentProviderClient contentProviderClient =
        contentResolver.acquireContentProviderClient(Constants.STATION_URI);
    try {
      contentProviderClient.update(Constants.STATION_URI, contentValues, null, null);
    } catch (RemoteException ex) {
      Log.e("Rollout", "Failed to update after new action");
    } finally {
      contentProviderClient.release();
    }
  }
}
