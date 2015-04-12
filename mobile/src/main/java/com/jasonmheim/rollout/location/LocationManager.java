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

package com.jasonmheim.rollout.location;

import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.jasonmheim.rollout.Constants;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.jasonmheim.rollout.Constants.MS_PER_MINUTE;
import static java.lang.Math.min;

/**
 * Manages the last known location of the device. The incoming value should normally be set by the
 * {@link LocationUpdateIntentService}, and all updates in turn notify the central content provider.
 */
@Singleton
public class LocationManager {

  static final String LATITUDE = "LastLatitude";
  static final String LONGITUDE = "LastLongitude";
  static final String TIMESTAMP = "LastLocationTimestamp";

  private final Application application;
  private final ContentResolver contentResolver;
  private final ExecutorService executorService;
  private final FusedLocationProviderApi fusedLocationProviderApi;
  private final Provider<GoogleApiClient> locationClientProvider;
  private final SharedPreferences sharedPreferences;

  @Inject
  LocationManager(
      Application application,
      ContentResolver contentResolver,
      ExecutorService executorService,
      FusedLocationProviderApi fusedLocationProviderApi,
      Provider<GoogleApiClient> locationClientProvider,
      SharedPreferences sharedPreferences) {
    this.application = application;
    this.contentResolver = contentResolver;
    this.executorService = executorService;
    this.fusedLocationProviderApi = fusedLocationProviderApi;
    this.locationClientProvider = locationClientProvider;
    this.sharedPreferences = sharedPreferences;
  }

  /**
   * Update the current location and notify the central content provider so that interested parties
   * can be updated. Intended for use only by {@link LocationUpdateIntentService}, as such this is
   * private to the package.
   */
  void setLastLocation(Location location) {
    Log.i("Rollout", "Storing location " + location.getLatitude() + " " + location.getLongitude());
    // TODO: Using shared preferences for this is a bit of a hack. It will trigger anything that
    // listens for a change to the settings even though no actual settings are changed. This may
    // unintentionally consume double cycles if something is listening to both a settings change and
    // a content provider update.
    sharedPreferences.edit()
        .putString(LATITUDE, Double.toString(location.getLatitude()))
        .putString(LONGITUDE, Double.toString(location.getLongitude()))
        .putLong(TIMESTAMP, location.getTime())
        .apply();
    notifyContentProvider();
  }

  /**
   * Retrieves the last recorded location, or {@code null} if it has never been known. This is
   * retrieved from storage rather than a GMS location client, and thus may be quite old, so callers
   * should be sure to check the time that the location was recorded if recency is important.
   */
  public Location getLastLocation() {
    // TODO: consider adding synchronization or document why it's unnecessary.
    if (sharedPreferences.contains(LATITUDE) && sharedPreferences.contains(LONGITUDE)) {
      Location location = new Location("");
      location.setLatitude(Double.parseDouble(sharedPreferences.getString(LATITUDE, "0")));
      location.setLongitude(Double.parseDouble(sharedPreferences.getString(LONGITUDE, "0")));
      location.setTime(sharedPreferences.getLong(TIMESTAMP, 0));
      return location;
    }
    return null;
  }

  private void notifyContentProvider() {
    // TODO: use LOCATION_URI instead of STATION_URI; drop the content values.
    ContentValues contentValues = new ContentValues();
    contentValues.put(Constants.UPDATE_KEY_LOCATION, true);
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

  /**
   * Submits a request to set the location update interval and accuracy. Typically this is done when
   * the user has updated their action. Set to slow values for idle/silent actions, and fast values
   * for search/ride actions.
   */
  public void setLocationUpdateInterval(final double intervalInMinutes, final int accuracy) {
    Log.i("Rollout", "Location update interval in minutes: " + intervalInMinutes);
    final long intervalInMs = (long) (intervalInMinutes * MS_PER_MINUTE);

    // The "fastest interval" should be less than the interval. Set it to the lesser of one minute
    // or half the requested interval.
    long halfIntervalInMs = intervalInMs / 2;
    final long fastestIntervalInMs = min(halfIntervalInMs, MS_PER_MINUTE);

    // Since we can't guarantee that the location client will be connected, run this on an executor
    // where we can perform a blocking connect to the location client.
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        LocationRequest locationRequest = new LocationRequest()
            .setFastestInterval(fastestIntervalInMs)
            .setInterval(intervalInMs)
            .setPriority(accuracy);

        Context context = application.getApplicationContext();
        Intent updateIntent = new Intent(context, LocationUpdateIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(
            context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        GoogleApiClient locationClient = locationClientProvider.get();
        locationClient.blockingConnect();
        fusedLocationProviderApi.requestLocationUpdates(
            locationClient, locationRequest, pendingIntent);
      }
    });
  }
}
