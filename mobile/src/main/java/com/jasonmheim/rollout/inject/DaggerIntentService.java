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

package com.jasonmheim.rollout.inject;

import android.app.IntentService;
import android.content.Intent;

/**
 * Base class for an intent service that relies on Dagger to inject fields. Presumes that the
 * containing application implements {@link ObjectGraphProvider}.
 * <p>
 * Subclasses that override {@link #onStartCommand} <b>must</b> invoke the superclass method in
 * order to trigger injection.
 */
public abstract class DaggerIntentService extends IntentService {

  public DaggerIntentService(String name) {
    super(name);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    ((ObjectGraphProvider) getApplication()).get().inject(this);
    return super.onStartCommand(intent, flags, startId);
  }
}
