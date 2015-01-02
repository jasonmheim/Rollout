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

package com.jasonmheim.rollout.module;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.action.ActionIntentService;
import com.jasonmheim.rollout.location.LocationUpdateIntentService;
import com.jasonmheim.rollout.station.CoreContentProvider;
import com.jasonmheim.rollout.station.StationData;
import com.jasonmheim.rollout.station.StationDataProcessor;
import com.jasonmheim.rollout.location.LocationConnectionCallbacks;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.settings.Settings;
import com.jasonmheim.rollout.sync.StationDataSyncAdapter;
import com.squareup.otto.Bus;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.jasonmheim.rollout.Constants.PREF_KEY;
import static com.jasonmheim.rollout.Constants.PREF_MODE;

/**
 * Primary dependency injection module for the entire application.
 */
@Module(
    library = true,
    injects = {
        Application.class,
        GoogleApiClient.class,
        LocationUpdateIntentService.class,
        LocationManager.class,
        ActionIntentService.class,
        ActionManager.class,
        Settings.class,
        StationDataProcessor.class,
        CoreContentProvider.class,
        StationDataSyncAdapter.class,
    }
)
public class ApplicationModule {
  private final Application application;

  public ApplicationModule(Application application) {
    this.application = application;
  }

  @Provides
  @Singleton
  Application provideApplication() {
    return application;
  }

  @Provides
  @Singleton
  ContentResolver provideContentResolver(Application application) {
    return application.getContentResolver();
  }

  @Provides
  @StationData
  File provideStationDataFile(Application application) {
    File filesDir = application.getApplicationContext().getFilesDir();
    return new File(filesDir, "stationData.json");
  }

  @Provides
  @StationData
  URLConnection provideStationDataFile() {
    try {
      return Constants.STATION_DATA_URL.openConnection();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Provides
  @Singleton
  Bus provideBus() {
    return new Bus();
  }

  @Provides
  @Singleton
  GoogleApiClient.ConnectionCallbacks provideConnectionCallbacks(
      LocationConnectionCallbacks connectionCallbacks) {
    return connectionCallbacks;
  }

  // TODO: build custom serializer and deserializer for better performance
  @Provides
  @Singleton
  Gson getGson() {
    return new Gson();
  }

  @Provides
  @Singleton
  GoogleApiClient.OnConnectionFailedListener provideConnectionFailedListener() {
    return new GoogleApiClient.OnConnectionFailedListener() {
      @Override
      public void onConnectionFailed(ConnectionResult connectionResult) {
      }
    };
  }

  @Provides
  Date provideDate() {
    return new Date();
  }

  @Provides
  @Singleton
  LocationManager provideLocationManager() {
    return (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
  }

  @Provides
  @Singleton
  ExecutorService provideExecutorService() {
    return Executors.newFixedThreadPool(10);
  }

  @Provides
  @Singleton
  GoogleApiClient provideLocationClient(
      Application context,
      GoogleApiClient.ConnectionCallbacks connectionCallbacks,
      GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
    return new GoogleApiClient.Builder(context)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(connectionCallbacks)
        .addOnConnectionFailedListener(connectionFailedListener)
        .build();
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences(Application application) {
    return application.getSharedPreferences(PREF_KEY, PREF_MODE);
  }

  @Provides
  @Singleton
  NotificationManager provideNotificationManager(Application application) {
    return (NotificationManager) application.getApplicationContext()
        .getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
