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

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.jasonmheim.rollout.inject.DaggerIntentService;
import com.jasonmheim.rollout.inject.ObjectGraphProvider;

import javax.inject.Inject;

/**
 * A simple intent service to handle an incoming update to location. Updates are handed off to the
 * {@link LocationManager}.
 */
public class LocationUpdateIntentService extends DaggerIntentService {

  @Inject
  LocationManager locationManager;

  public LocationUpdateIntentService() {
    super("LocationUpdateIntentService");
  }

  /** Test only constructor */
  LocationUpdateIntentService(LocationManager locationManager) {
    this();
    this.locationManager = locationManager;
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent == null) {
      return;
    }
    Bundle extras = intent.getExtras();
    if (extras == null) {
      return;
    }
    Location location =
        intent.getExtras().getParcelable(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
    if (location != null) {
      locationManager.setLastLocation(location);
    }
  }
}
