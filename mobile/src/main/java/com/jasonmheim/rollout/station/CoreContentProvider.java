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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.action.ActionIntentService;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.data.Station;
import com.jasonmheim.rollout.data.StationDistance;
import com.jasonmheim.rollout.data.StationDistanceRank;
import com.jasonmheim.rollout.data.StationList;
import com.jasonmheim.rollout.inject.ObjectGraphProvider;
import com.jasonmheim.rollout.location.LocationManager;
import com.jasonmheim.rollout.settings.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;

import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;
import static com.jasonmheim.rollout.Constants.ACCOUNT;
import static com.jasonmheim.rollout.Constants.ACTION_IDLE;
import static com.jasonmheim.rollout.Constants.ACTION_RIDE;
import static com.jasonmheim.rollout.Constants.ACTION_SEARCH;
import static com.jasonmheim.rollout.Constants.ACTION_SILENCE;
import static com.jasonmheim.rollout.Constants.AUTHORITY;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_HOME;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_WORK;
import static com.jasonmheim.rollout.Constants.UPDATE_KEY_ACTION;
import static com.jasonmheim.rollout.Constants.UPDATE_KEY_DESTINATION;

/**
 * This class serves as the hub for all data and notification of updates to pretty much everything
 * in the application.
 * <p>
 * Normally, a content provider would be a front for a local MySQL store. Such a solution would be
 * too heavyweight in this case, since the service with bike share data does not provide any
 * mechanism for incremental updates; the service exposes a blob of JSON data with the current state
 * of the entire bike share system.
 * <p>
 * Given this we simply serialize the entire blob to disk using {@link StationDataStorage}. The insert
 * method expects to be invoked by the Sync Adapter that polls for updates. The query method exposes
 * a custom StationDataCursor that has one row of data with one column: the entire JSON blob.
 * <p>
 * In addition to the bike share data being fed here from the Sync Adapter, the update method is
 * invoked when a new current location is known, or the user has updated the application's action.
 * The former is to allow for notifications to be posted even if the main app is not running. The
 * latter is to be able to handle pending intents from notifications including the wearable.
 * <p>
 * By having all updates posted here, two things are accomplished:
 * <ul>
 *   <li>This class becomes the hub of all notifications.
 *   <li>The UI can listen for updates from a single STATION_URI to get the effects of all changes.
 * </ul>
 */
public class CoreContentProvider extends ContentProvider {

  @Inject
  Gson gson;

  @Inject
  StationDataStorage stationDataStorage;

  @Inject
  StationDataDownloader stationListDownloader;

  @Inject
  ExecutorService executorService;

  @Inject
  ActionManager actionManager;

  @Inject
  NotificationManager notificationManager;

  @Inject
  LocationManager locationManager;

  @Inject
  Settings settings;

  @Inject
  StationDataProcessor stationDataProcessor;

  private StationList stationList;
  private StationDistanceRank previousStationDistanceRank = null;

  private static final int[] FILL_COLOR = {
      R.drawable.ic_fill_0_color_48dp,
      R.drawable.ic_fill_1_color_48dp,
      R.drawable.ic_fill_2_color_48dp,
      R.drawable.ic_fill_3_color_48dp,
      R.drawable.ic_fill_4_color_48dp,
      R.drawable.ic_fill_5_color_48dp,
      R.drawable.ic_fill_6_color_48dp,
      R.drawable.ic_fill_7_color_48dp,
      R.drawable.ic_fill_8_color_48dp,
      R.drawable.ic_fill_0_red1_48dp,
      R.drawable.ic_fill_1_red1_48dp,
      R.drawable.ic_fill_2_red1_48dp,
      R.drawable.ic_fill_3_red1_48dp,
      R.drawable.ic_fill_4_red1_48dp,
      R.drawable.ic_fill_5_red1_48dp,
      R.drawable.ic_fill_6_red1_48dp,
      R.drawable.ic_fill_7_red1_48dp,
      R.drawable.ic_fill_8_red1_48dp,
      R.drawable.ic_fill_0_red2_48dp,
      R.drawable.ic_fill_1_red2_48dp,
      R.drawable.ic_fill_2_red2_48dp,
      R.drawable.ic_fill_3_red2_48dp,
      R.drawable.ic_fill_4_red2_48dp,
      R.drawable.ic_fill_5_red2_48dp,
      R.drawable.ic_fill_6_red2_48dp,
      R.drawable.ic_fill_7_red2_48dp,
      R.drawable.ic_fill_8_red2_48dp,
      R.drawable.ic_fill_0_red3_48dp,
      R.drawable.ic_fill_1_red3_48dp,
      R.drawable.ic_fill_2_red3_48dp,
      R.drawable.ic_fill_3_red3_48dp,
      R.drawable.ic_fill_4_red3_48dp,
      R.drawable.ic_fill_5_red3_48dp,
      R.drawable.ic_fill_6_red3_48dp,
      R.drawable.ic_fill_7_red3_48dp,
      R.drawable.ic_fill_8_red3_48dp,
  };

  private static final int[] FILL_BW = {
      R.drawable.ic_fill_0_bw_24p,
      R.drawable.ic_fill_1_bw_24p,
      R.drawable.ic_fill_2_bw_24p,
      R.drawable.ic_fill_3_bw_24p,
      R.drawable.ic_fill_4_bw_24p,
      R.drawable.ic_fill_5_bw_24p,
      R.drawable.ic_fill_6_bw_24p,
      R.drawable.ic_fill_7_bw_24p,
      R.drawable.ic_fill_8_bw_24p,
      R.drawable.ic_fill_0_bw_red1_24p,
      R.drawable.ic_fill_1_bw_red1_24p,
      R.drawable.ic_fill_2_bw_red1_24p,
      R.drawable.ic_fill_3_bw_red1_24p,
      R.drawable.ic_fill_4_bw_red1_24p,
      R.drawable.ic_fill_5_bw_red1_24p,
      R.drawable.ic_fill_6_bw_red1_24p,
      R.drawable.ic_fill_7_bw_red1_24p,
      R.drawable.ic_fill_8_bw_red1_24p,
      R.drawable.ic_fill_0_bw_red2_24p,
      R.drawable.ic_fill_1_bw_red2_24p,
      R.drawable.ic_fill_2_bw_red2_24p,
      R.drawable.ic_fill_3_bw_red2_24p,
      R.drawable.ic_fill_4_bw_red2_24p,
      R.drawable.ic_fill_5_bw_red2_24p,
      R.drawable.ic_fill_6_bw_red2_24p,
      R.drawable.ic_fill_7_bw_red2_24p,
      R.drawable.ic_fill_8_bw_red2_24p,
      R.drawable.ic_fill_0_bw_red3_24p,
      R.drawable.ic_fill_1_bw_red3_24p,
      R.drawable.ic_fill_2_bw_red3_24p,
      R.drawable.ic_fill_3_bw_red3_24p,
      R.drawable.ic_fill_4_bw_red3_24p,
      R.drawable.ic_fill_5_bw_red3_24p,
      R.drawable.ic_fill_6_bw_red3_24p,
      R.drawable.ic_fill_7_bw_red3_24p,
      R.drawable.ic_fill_8_bw_red3_24p,
  };

  // Use this to bump the priority of a notification without actually buzzing the device
  private static final long[] BUZZ_SILENT = {
      0,
      0,
  };

  private static final long[] BUZZ_0 = {
      0,
      200,
      100,
      200,
      100,
      200,
  };

  private static final long[] BUZZ_1 = {
      0,
      100,
      150,
      200,
      150,
      300,
      150,
      400,
  };

  private static final long[] BUZZ_2 = {
      0,
      100,
      150,
      200,
      150,
      300,
      150,
      400,
      250,
      100,
      150,
      200,
      150,
      300,
      150,
      400,
  };

  private static final long[] BUZZ_3 = {
      0,
      100,
      150,
      200,
      150,
      300,
      150,
      400,
      250,
      100,
      150,
      200,
      150,
      300,
      150,
      400,
      250,
      100,
      150,
      200,
      150,
      300,
      150,
      400,
  };

  private static final long[][] BUZZ_RANKS = {
      BUZZ_0, BUZZ_1, BUZZ_2, BUZZ_3,
  };

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public String getType(Uri uri) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    // TODO: make this a constant, or even better, create static helper methods
    String valuesAsString = values.getAsString("StationList");
    try {
      StationList newStationList = gson.fromJson(valuesAsString, StationList.class);
      internalInsert(newStationList);
    } catch (RuntimeException ex) {
      Log.e("Rollout", "Insert deserialization exception", ex);
    }
    return Constants.STATION_URI;
  }

  @Override
  public boolean onCreate() {
    ((ObjectGraphProvider) getContext().getApplicationContext()).get().inject(this);
    return true;
  }

  @Override
  public Cursor query(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder) {

    StationDataCursor cursor = new StationDataCursor();
    if (stationList == null) {
      Log.i("Rollout", "Provider not yet initialized, checking local storage...");
      stationList = stationDataStorage.get();
    }
    if (stationList == null) {
      Log.i("Rollout", "Local storage was empty.");
      Future<StationList> future = executorService.submit(stationListDownloader);
      try {
        StationList providerList = future.get();
        if (providerList == null) {
          Log.i("Rollout", "Station list failed to download from query, requesting sync");
          Bundle settingsBundle = new Bundle();
          settingsBundle.putBoolean(
              ContentResolver.SYNC_EXTRAS_MANUAL, true);
          settingsBundle.putBoolean(
              ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
          ContentResolver.requestSync(ACCOUNT, AUTHORITY, settingsBundle);
        } else {
          Log.i("Rollout", "Direct download succeeded.");
          // This implicitly sets stationList
          internalInsert(providerList);
        }
      } catch (InterruptedException ex) {
        Log.w("Rollout", "Content provider sync was interrupted", ex);
      } catch (ExecutionException ex) {
        Log.w("Rollout", "Content provider sync execution failed", ex);
      }
    }
    if (stationList != null) {
      cursor.setStationDataJson(gson.toJson(stationList));
    }
    cursor.setNotificationUri(getContext().getContentResolver(), Constants.STATION_URI);
    return cursor;
  }

  @Override
  public int update(
      Uri uri,
      ContentValues values,
      String selection,
      String[] selectionArgs) {
    // TODO: this is silly. Disambiguate by using different URIs for data, location, action, etc
    if (values.containsKey(Constants.UPDATE_KEY_LOCATION)) {
      Log.i("Rollout", "Updating location");
      internalUpdate();
    } else if (values.containsKey(Constants.UPDATE_KEY_ACTION)) {
      // TODO: Do this in the ActionManager. It's ridiculous to do this here.
      int action = actionManager.getAction();
      Log.i("Rollout", "Updating action to " + action);
      switch (action) {
        case Constants.ACTION_SEARCH:
          Log.i("Rollout", "Active search action");
          // Medium location speed
          locationManager.setLocationUpdateInterval(1, PRIORITY_HIGH_ACCURACY);
          // Fast periodic sync
          setSyncPeriod(1);
          break;
        case Constants.ACTION_RIDE:
          Log.i("Rollout", "Riding action");
          // Fast location speed
          locationManager.setLocationUpdateInterval(0.5, PRIORITY_HIGH_ACCURACY);
          // Fast periodic sync
          setSyncPeriod(1);
          break;
        case Constants.ACTION_IDLE:
          Log.i("Rollout", "Passive search action");
          // Slow location speed
          locationManager.setLocationUpdateInterval(10, PRIORITY_BALANCED_POWER_ACCURACY);
          // Slow periodic sync
          setSyncPeriod(5);
          break;
        case Constants.ACTION_SILENCE:
          Log.i("Rollout", "Muted action");
          // Extra slow location speed
          locationManager.setLocationUpdateInterval(60, PRIORITY_BALANCED_POWER_ACCURACY);
          // Extra slow periodic sync
          setSyncPeriod(20);
      }
      internalUpdate();
    }
    return 0;
  }

  private void setSyncPeriod(double periodInMinutes) {
    Log.i("Rollout", "Data update period in minutes: " + periodInMinutes);
    ContentResolver.addPeriodicSync(
        ACCOUNT, AUTHORITY, Bundle.EMPTY, (long) (periodInMinutes * Constants.SECONDS_PER_MINUTE));
  }

  private synchronized void internalUpdate() {
    if (!settings.isDisclaimerAgreed()) {
      // The user has not yet agreed to the disclaimer. Post no notifications or URI changes.
      notificationManager.cancel(1);
      return;
    }
    try {
      int action = actionManager.getAction();
      if (action == ACTION_SILENCE) {
        // Turn off notifications but still post URI changes via finally clause
        notificationManager.cancel(1);
        return;
      }
      if (stationList == null) {
        return;
      }
      Location lastLocation = locationManager.getLastLocation();
      if (lastLocation == null) {
        return;
      }
      StationDistanceRank stationDistanceRank
          = stationDataProcessor.getClosestAvailableStation(stationList);
      if (stationDistanceRank == null) {
        return;
      }
      try {
        Station station = stationDistanceRank.getStationDistance().getStation();
        int iconIndex = getIconIndex(stationDistanceRank);

        // The color icons look better on the wearable with their white background
        Notification.WearableExtender wearable = new Notification.WearableExtender()
            .setContentIcon(FILL_COLOR[iconIndex])
            .setHintHideIcon(true);

        // Don't bother setting large icon, it's redundant with the small icon especially once you
        // are on Lollipop.
        Notification.Builder builder = new Notification.Builder(getContext())
            .setSmallIcon(FILL_BW[iconIndex])
            .setColor(getContext().getResources().getColor(R.color.availableBikes))
            .setContentTitle(station.stationName);
        switch (action) {
          case ACTION_RIDE:
            builder.setContentText(getDocks(stationDistanceRank))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(getAppropriateBuzz(stationDistanceRank));
            wearable.addAction(getIdleAction())
                .addAction(getSearchAction())
                .addAction(getSilenceAction());
            break;
          case ACTION_SEARCH:
            builder.setContentText(getBikesAndDuds(stationDistanceRank))
                .setVibrate(BUZZ_SILENT)
                .setPriority(NotificationCompat.PRIORITY_MAX);
            wearable.addActions(getRideActions())
                .addAction(getIdleAction())
                .addAction(getSilenceAction());
            break;
          case ACTION_IDLE:
            builder.setContentText(getBikesAndDuds(stationDistanceRank))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            wearable.addAction(getSearchAction())
                .addActions(getRideActions())
                .addAction(getSilenceAction());
            break;
        }
        Intent resultIntent = new Intent(getContext(), StationDataActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            getContext(),
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent)
            .extend(wearable);

        notificationManager.notify(1, builder.build());
      } finally {
        previousStationDistanceRank = stationDistanceRank;
      }
    } finally {
      getContext().getContentResolver().notifyChange(Constants.STATION_URI, null);
    }
  }

  private long[] getAppropriateBuzz(StationDistanceRank nextStationDistanceRank) {
    if (settings.isVibrationEnabled() && actionManager.getDestinationName() != null) {
      if (previousStationDistanceRank == null) {
        return BUZZ_RANKS[nextStationDistanceRank.getLimitedRank()];
      }
      Station previousStation = previousStationDistanceRank.getStationDistance().getStation();
      Station nextStation = nextStationDistanceRank.getStationDistance().getStation();
      if (previousStation.availableBikes != nextStation.availableBikes
          || previousStation.availableDocks != nextStation.availableDocks
          || previousStation.id != nextStation.id) {
        return BUZZ_RANKS[nextStationDistanceRank.getLimitedRank()];
      }
    }
    return BUZZ_SILENT;
  }

  private static int getIconIndex(StationDistanceRank stationDistanceRank) {
    Station station = stationDistanceRank.getStationDistance().getStation();
    int limitedRank = stationDistanceRank.getLimitedRank();
    int iconIndex;
    if (station.availableBikes == 0) {
      iconIndex = 0;
    } else if (station.availableDocks == 0) {
      iconIndex = 8;
    } else {
      int max = station.availableDocks + station.availableBikes;
      iconIndex = ((station.availableBikes * 7) / max) + 1;
    }
    // Adjust index if the current rank is > 0.
    return iconIndex + (limitedRank * 9);
  }


  private static String getBikesAndDuds(StationDistanceRank stationDistanceRank) {
    StationDistance stationDistance = stationDistanceRank.getStationDistance();
    Station station = stationDistance.getStation();
    int duds = station.totalDocks - (station.availableDocks + station.availableBikes);
    return getRankString(stationDistanceRank.getRank())
        + "Bikes: " + station.availableBikes + " Duds: " + duds + "\n"
        + "Go: " + stationDistance.getDistanceString();
  }

  private static String getDocks(StationDistanceRank stationDistanceRank) {
    StationDistance stationDistance = stationDistanceRank.getStationDistance();
    return getRankString(stationDistanceRank.getRank())
        + "Docks: " + stationDistance.getStation().availableDocks + "\n"
        + "Go: " + stationDistance.getDistanceString();
  }

  private static String getRankString(int rank) {
    return rank == 0 ? "" : "Rank: " + (rank + 1) + "\n";
  }

  private void internalInsert(StationList newStationList) {
    if (newStationList == null) {
      return;
    }
    stationList = newStationList;
    stationDataStorage.set(stationList);
    internalUpdate();
  }

  private Notification.Action getSearchAction() {
    return new Notification.Action(
        R.drawable.ic_search_white_24dp,
        "Search",
        getActionSettingIntent(ACTION_SEARCH));
  }

  private Notification.Action getIdleAction() {
    return new Notification.Action(
        R.drawable.ic_pause_white_24dp,
        "Idle",
        getActionSettingIntent(ACTION_IDLE));
  }

  private List<Notification.Action> getRideActions() {
    List<Notification.Action> actions = new ArrayList<Notification.Action>();
    if (settings.isHomeDestinationActive()) {
      actions.add(new Notification.Action(
          R.drawable.ic_directions_bike_white_24dp,
          DESTINATION_NAME_HOME,
          getRideActionWithDestinationIntent(DESTINATION_NAME_HOME)));
    }
    if (settings.isWorkDestinationActive()) {
      actions.add(new Notification.Action(
          R.drawable.ic_directions_bike_white_24dp,
          DESTINATION_NAME_WORK,
          getRideActionWithDestinationIntent(DESTINATION_NAME_WORK)));
    }
    actions.add(new Notification.Action(
        R.drawable.ic_directions_bike_white_24dp,
        "Roam",
        getActionSettingIntent(ACTION_RIDE)));
    return actions;
  }

  private Notification.Action getSilenceAction() {
    return new Notification.Action(
        R.drawable.ic_volume_off_white_24dp,
        "Mute",
        getActionSettingIntent(ACTION_SILENCE));
  }

  private PendingIntent getActionSettingIntent(int action) {
    Uri data = Constants.STATION_URI.buildUpon()
        // TODO: Don't use update keys for query param names
        .appendQueryParameter(UPDATE_KEY_ACTION, Integer.toString(action))
        .build();
    Intent intent = new Intent(getContext(), ActionIntentService.class);
    intent.setData(data);

    PendingIntent pendingIntent = PendingIntent.getService(
        getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    return pendingIntent;
  }

  private PendingIntent getRideActionWithDestinationIntent(String destination) {
    Uri data = Constants.STATION_URI.buildUpon()
        .appendQueryParameter(UPDATE_KEY_ACTION, Integer.toString(ACTION_RIDE))
        .appendQueryParameter(UPDATE_KEY_DESTINATION, destination)
        .build();
    Intent intent = new Intent(getContext(), ActionIntentService.class);
    intent.setData(data);

    PendingIntent pendingIntent = PendingIntent.getService(
        getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    return pendingIntent;
  }
}
