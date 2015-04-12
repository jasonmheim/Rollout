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
import android.content.ContentProvider;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.station.CoreContentProvider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.tester.android.content.TestSharedPreferences;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Provider;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@TargetApi(21)
@Config(manifest = Config.NONE)
public class LocationManagerTest {

  private ShadowApplication shadowApplication;
  private ShadowContentResolver shadowContentResolver;
  private ExecutorService sameThreadExecutor;
  @Mock private FusedLocationProviderApi mockFusedLocationProviderApi;
  @Mock private GoogleApiClient mockLocationClient;
  private TestSharedPreferences testSharedPreferences;
  @Mock private ContentProvider mockContentProvider;

  private LocationManager instance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    shadowApplication = Robolectric.getShadowApplication();
    shadowContentResolver = Robolectric.shadowOf(Robolectric.application.getContentResolver());
    sameThreadExecutor = MoreExecutors.newDirectExecutorService();
    Map<String, Map<String, Object>> preferenceMap = Maps.newHashMap();
    testSharedPreferences = new TestSharedPreferences(preferenceMap, "prefs", 0);

    ShadowContentResolver.registerProvider(Constants.AUTHORITY, mockContentProvider);

    instance = new LocationManager(Robolectric.application,
        Robolectric.application.getContentResolver(),
        sameThreadExecutor,
        mockFusedLocationProviderApi,
        new Provider<GoogleApiClient>() {
          @Override public GoogleApiClient get() { return mockLocationClient; }
        },
        testSharedPreferences);


  }

  @Test
  public void testSetLastLocation() throws Exception {
    testSharedPreferences.edit()
        .putString(LocationManager.LATITUDE, "1.23")
        .putString(LocationManager.LONGITUDE, "4.56")
        .putLong(LocationManager.TIMESTAMP, 78907890L)
        .apply();
    Location location = new Location("testonly");
    location.setLatitude(32.1);
    location.setLongitude(-43.2);
    location.setTime(987987987L);
    instance.setLastLocation(location);

    assertEquals("32.1", testSharedPreferences.getString(LocationManager.LATITUDE, ""));
    assertEquals("-43.2", testSharedPreferences.getString(LocationManager.LONGITUDE, ""));
    assertEquals(987987987L, testSharedPreferences.getLong(LocationManager.TIMESTAMP, 0L));
  }

  @Test
  public void testGetLastLocation() throws Exception {
    testSharedPreferences.edit()
        .putString(LocationManager.LATITUDE, "4.56")
        .putString(LocationManager.LONGITUDE, "1.23")
        .putLong(LocationManager.TIMESTAMP, 98769876L)
        .apply();
    Location location = instance.getLastLocation();
    Location expected = new Location("");
    expected.setLatitude(4.56);
    expected.setLongitude(1.23);
    expected.setTime(98769876L);

    assertEquals(expected, location);
  }

  // TODO(jasonmheim): add a test for setLocationUpdateInterval
}