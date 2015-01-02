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

import com.jasonmheim.rollout.inject.DaggerIntentService;

import javax.inject.Inject;

import static com.jasonmheim.rollout.Constants.UPDATE_KEY_ACTION;
import static com.jasonmheim.rollout.Constants.UPDATE_KEY_DESTINATION;

/**
 * A simple intent service to handle an incoming update to action. Updates are handed off to the
 * {@link ActionManager}.
 */
public class ActionIntentService extends DaggerIntentService {

  @Inject
  ActionManager actionManager;

  public ActionIntentService() {
    super("ActionIntentService");
  }

  /** Test only constructor */
  ActionIntentService(ActionManager actionManager) {
    this();
    this.actionManager = actionManager;
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent == null) {
      return;
    }
    Uri data = intent.getData();
    if (data == null) {
      return;
    }
    String actionString = data.getQueryParameter(UPDATE_KEY_ACTION);
    if (actionString == null) {
      return;
    }
    int action = Integer.parseInt(actionString);
    String destination = data.getQueryParameter(UPDATE_KEY_DESTINATION);
    if (destination == null) {
      actionManager.setAction(action);
    } else {
      actionManager.setAction(action, destination);
    }
  }
}
