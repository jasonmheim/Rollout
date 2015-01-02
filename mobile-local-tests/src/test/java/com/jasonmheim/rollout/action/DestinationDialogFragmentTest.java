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
import android.app.AlertDialog;
import android.os.Bundle;

import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.settings.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.jasonmheim.rollout.Constants.ACTION_RIDE;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_HOME;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_WORK;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@TargetApi(21)
@Config(manifest = Config.NONE)
public class DestinationDialogFragmentTest {

  @Mock private ActionManager mockActionManager;
  @Mock private Settings mockSettings;

  private DestinationDialogFragment instance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    instance = new DestinationDialogFragment(mockActionManager, mockSettings);
  }

  @Test
  public void testGetOptions_noWorkNoHome() throws Exception {
    setHomeInactive();
    setWorkInactive();
    assertEquals(1, instance.getOptions().length);
  }

  @Test
  public void testGetOptions_withWorkNoHome() throws Exception {
    setHomeInactive();
    setWorkActive();
    String[] options = instance.getOptions();
    assertEquals(2, options.length);
    assertEquals(Constants.DESTINATION_NAME_WORK, options[0]);
  }

  @Test
  public void testGetOptions_noWorkWithHome() throws Exception {
    setHomeActive();
    setWorkInactive();
    String[] options = instance.getOptions();
    assertEquals(2, options.length);
    assertEquals(DESTINATION_NAME_HOME, options[0]);
  }

  @Test
  public void testGetOptions_withWorkWithHome() throws Exception {
    setHomeActive();
    setWorkActive();
    String[] options = instance.getOptions();
    assertEquals(3, options.length);
    assertEquals(DESTINATION_NAME_HOME, options[0]);
    assertEquals(Constants.DESTINATION_NAME_WORK, options[1]);
  }

  @Test
  public void testOnClick_home() throws Exception {
    setHomeActive();
    setWorkActive();
    instance.onClick(null, 0);
    verify(mockActionManager).setAction(ACTION_RIDE, DESTINATION_NAME_HOME);
  }

  @Test
  public void testOnClick_work() throws Exception {
    setHomeActive();
    setWorkActive();
    instance.onClick(null, 1);
    verify(mockActionManager).setAction(ACTION_RIDE, DESTINATION_NAME_WORK);
  }

  @Test
  public void testOnClick_roam() throws Exception {
    setHomeActive();
    setWorkActive();
    instance.onClick(null, 2);
    verify(mockActionManager).setAction(ACTION_RIDE);
  }

  @Test
  public void testOnClick_outOfBounds() throws Exception {
    setHomeActive();
    setWorkActive();
    instance.onClick(null, -1);
    verify(mockActionManager).setAction(ACTION_RIDE);
  }

  private void setHomeActive() {
    Mockito.when(mockSettings.isHomeDestinationActive()).thenReturn(true);
  }

  private void setWorkActive() {
    Mockito.when(mockSettings.isWorkDestinationActive()).thenReturn(true);
  }

  private void setHomeInactive() {
    Mockito.when(mockSettings.isHomeDestinationActive()).thenReturn(false);
  }

  private void setWorkInactive() {
    Mockito.when(mockSettings.isWorkDestinationActive()).thenReturn(false);
  }
}