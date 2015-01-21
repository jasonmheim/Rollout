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

import android.annotation.TargetApi;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Provider;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@TargetApi(21)
@Config(manifest = Config.NONE)
public class LocationConnectionCallbacksTest {

  @Mock private FusedLocationProviderApi mockFusedLocationProviderApi;
  @Mock private GoogleApiClient mockLocationClient;
  @Mock private LocationManager mockLocationManager;

  private LocationConnectionCallbacks instance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    instance = new LocationConnectionCallbacks(
        mockFusedLocationProviderApi,
        new Provider<GoogleApiClient>() {
          @Override
          public GoogleApiClient get() {
            return mockLocationClient;
          }
        },
        mockLocationManager);
  }

  @Test
  public void testOnConnected() throws Exception {
    Location location = new Location("dummy");
    when(mockFusedLocationProviderApi.getLastLocation(mockLocationClient)).thenReturn(location);

    instance.onConnected(null);
    verify(mockLocationManager).setLastLocation(location);
  }
}