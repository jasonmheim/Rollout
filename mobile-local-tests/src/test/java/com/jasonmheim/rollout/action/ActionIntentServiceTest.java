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

import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.jasonmheim.rollout.Constants.UPDATE_KEY_ACTION;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ActionIntentServiceTest {

  @Mock private ActionManager mockActionManager;

  private ActionIntentService instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    instance = new ActionIntentService(mockActionManager);
  }

  @Test
  public void testOnHandleIntent_nullSafe() throws Exception {
    instance.onHandleIntent(null);
    verifyZeroInteractions(mockActionManager);
  }

  @Test
  public void testOnHandleIntent_noData() throws Exception {
    Intent intent = new Intent();
    instance.onHandleIntent(intent);
    verifyZeroInteractions(mockActionManager);
  }

  @Test
  public void testOnHandleIntent_noAction() throws Exception {
    Intent intent = new Intent();
    intent.setData(new Uri.Builder().build());
    instance.onHandleIntent(intent);
    verifyZeroInteractions(mockActionManager);
  }

  @Test
  public void testOnHandleIntent_withAction() throws Exception {
    Intent intent = new Intent();
    intent.setData(new Uri.Builder().appendQueryParameter(UPDATE_KEY_ACTION, "1").build());
    instance.onHandleIntent(intent);
    verify(mockActionManager).setAction(1);
  }
}