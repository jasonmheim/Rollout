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

import android.annotation.TargetApi;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;

import com.jasonmheim.rollout.settings.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.jasonmheim.rollout.Constants.ACTION_IDLE;
import static com.jasonmheim.rollout.Constants.ACTION_RIDE;
import static com.jasonmheim.rollout.Constants.ACTION_SEARCH;
import static com.jasonmheim.rollout.Constants.ACTION_SILENCE;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_HOME;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_WORK;
import static com.jasonmheim.rollout.Constants.PREF_KEY;
import static com.jasonmheim.rollout.Constants.PREF_MODE;
import static com.jasonmheim.rollout.Constants.STATION_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link ActionManager}.
 */
@RunWith(RobolectricTestRunner.class)
@TargetApi(21)
@Config(manifest = Config.NONE)
public class ActionManagerTest {

  @Mock private ContentResolver mockContentResolver;
  @Mock private Settings mockSettings;

  @Mock private ContentProviderClient mockContentProviderClient;

  private SharedPreferences fakeSharedPreferences;

  private ActionManager instance;

  private Location homeLocation;
  private Location workLocation;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    homeLocation = new Location("");
    homeLocation.setLatitude(1.234);
    homeLocation.setLongitude(2.345);
    workLocation = new Location("");
    workLocation.setLatitude(4.321);
    workLocation.setLongitude(5.432);
    fakeSharedPreferences = Robolectric.application.getSharedPreferences(PREF_KEY, PREF_MODE);
    instance = new ActionManager(
        mockContentResolver,
        mockSettings,
        fakeSharedPreferences);

    Mockito.when(mockContentResolver.acquireContentProviderClient(STATION_URI))
        .thenReturn(mockContentProviderClient);
    Mockito.when(mockSettings.getHomeDestination()).thenReturn(homeLocation);
    Mockito.when(mockSettings.getWorkDestination()).thenReturn(workLocation);
  }

  @Test
  public void testSetAction() throws Exception {
    instance.setAction(ACTION_RIDE);
    verifyUpdateNotification();
    verifyCurrentAction(ACTION_RIDE);
  }

  @Test
  public void testSetAction_withDestination() throws Exception {
    instance.setAction(ACTION_RIDE, "Home");
    verifyUpdateNotification();
    verifyCurrentAction(ACTION_RIDE, "Home");
  }

  @Test
  public void testSetAction_withNewAction() throws Exception {
    setCurrentAction(ACTION_IDLE);
    instance.setAction(ACTION_SEARCH);
    verifyUpdateNotification();
    verifyCurrentAction(ACTION_SEARCH);
  }

  @Test
  public void testSetAction_withNewDestination() throws Exception {
    setCurrentAction(ACTION_RIDE, "Home");
    instance.setAction(ACTION_RIDE, "Work");
    verifyUpdateNotification();
    verifyCurrentAction(ACTION_RIDE, "Work");
  }

  @Test
  public void testSetAction_withSameAction() throws Exception {
    setCurrentAction(ACTION_SILENCE);
    instance.setAction(ACTION_SILENCE);
    verifyUpdateNotInvoked();
    verifyCurrentAction(ACTION_SILENCE);
  }

  @Test
  public void testSetAction_withSameActionAndDestination() throws Exception {
    setCurrentAction(ACTION_RIDE, "Home");
    instance.setAction(ACTION_RIDE, "Home");
    verifyUpdateNotInvoked();
    verifyCurrentAction(ACTION_RIDE, "Home");
  }

  @Test
  public void testGetAction() throws Exception {
    setCurrentAction(ACTION_SILENCE);
    assertEquals(ACTION_SILENCE, instance.getAction());
  }

  @Test
  public void testGetDestinationName() throws Exception {
    setCurrentAction(ACTION_RIDE, "Home");
    assertEquals("Home", instance.getDestinationName());
  }

  @Test
  public void testGetDestination_null() throws Exception {
    setCurrentAction(ACTION_IDLE);
    assertNull(instance.getDestination());
  }

  @Test
  public void testGetDestination_unknown() throws Exception {
    setCurrentAction(ACTION_RIDE, "unknown");
    assertNull(instance.getDestination());
  }

  @Test
  public void testGetDestination_work() throws Exception {
    setCurrentAction(ACTION_RIDE, DESTINATION_NAME_WORK);
    assertEquals(workLocation, instance.getDestination());
  }

  @Test
  public void testGetDestination_home() throws Exception {
    setCurrentAction(ACTION_RIDE, DESTINATION_NAME_HOME);
    assertEquals(homeLocation, instance.getDestination());
  }

  @Test
  public void testGetActionDisplayName() throws Exception {
    // TODO: fill this out once the strings are exported properly.
  }

  private void verifyUpdateNotification() throws Exception {
    verify(mockContentProviderClient).update(
        Mockito.<Uri>any(), Mockito.<ContentValues>any(),
        Mockito.<String>any(), Mockito.<String[]>any());
  }

  private void verifyUpdateNotInvoked() throws Exception {
    verifyZeroInteractions(mockContentProviderClient);
  }

  private void setCurrentAction(int action) {
    fakeSharedPreferences.edit()
        .putInt(ActionManager.ACTION_KEY, action)
        .remove(ActionManager.DESTINATION_KEY)
        .apply();
  }

  private void setCurrentAction(int action, String destination) {
    fakeSharedPreferences.edit()
        .putInt(ActionManager.ACTION_KEY, action)
        .putString(ActionManager.DESTINATION_KEY, destination)
        .apply();
  }

  private void verifyCurrentAction(int action) {
    assertEquals(action, fakeSharedPreferences.getInt(ActionManager.ACTION_KEY, -123));
    assertFalse(fakeSharedPreferences.contains(ActionManager.DESTINATION_KEY));
  }

  private void verifyCurrentAction(int action, String destination) {
    assertEquals(action, fakeSharedPreferences.getInt(ActionManager.ACTION_KEY, -123));
    assertEquals(destination, fakeSharedPreferences.getString(ActionManager.DESTINATION_KEY, ""));
  }
}