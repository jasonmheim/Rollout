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
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderApi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@TargetApi(21)
@Config(manifest = Config.NONE)
public class LocationUpdateIntentServiceTest {

  @Mock private LocationManager mockLocationManager;

  private LocationUpdateIntentService instance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    instance = new LocationUpdateIntentService(mockLocationManager);
  }

  @Test
  public void testOnHandleIntent_nullSafe() throws Exception {
    instance.onHandleIntent(null);
    verifyZeroInteractions(mockLocationManager);
  }

  @Test
  public void testOnHandleIntent_extrasMissing() {
    Intent intent = new Intent();
    instance.onHandleIntent(intent);
    verifyZeroInteractions(mockLocationManager);
  }

  @Test
  public void testOnHandleIntent_locationMissing() {
    Location fakeLocation = new Location("dummy");
    fakeLocation.setLatitude(12.3456);
    fakeLocation.setLongitude(65.4321);
    Intent intent = new Intent();
    intent.putExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED, fakeLocation);

    instance.onHandleIntent(intent);
    verify(mockLocationManager).setLastLocation(fakeLocation);
  }
}