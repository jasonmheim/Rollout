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
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.jasonmheim.rollout.action.ActionManager;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Handles connection & disconnect from location client.
 */
@Singleton
public class LocationConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

  private final Provider<GoogleApiClient> locationClientProvider;
  private final LocationManager locationManager;

  @Inject
  public LocationConnectionCallbacks(
      Provider<GoogleApiClient> locationClientProvider,
      LocationManager locationManager) {
    this.locationClientProvider = locationClientProvider;
    this.locationManager = locationManager;
  }

  @Override
  public void onConnected(Bundle bundle) {
    // NB: While the location client is a singleton, we need the provider to avoid circular deps.
    GoogleApiClient locationClient = locationClientProvider.get();
    Location location = LocationServices.FusedLocationApi.getLastLocation(locationClient);
    locationManager.setLastLocation(location);
  }

  @Override
  public void onConnectionSuspended(int i) {
    // No-op
  }
}
