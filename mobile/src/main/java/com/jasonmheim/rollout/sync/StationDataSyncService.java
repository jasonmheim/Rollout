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

package com.jasonmheim.rollout.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This service exists only to bootstrap the sync adapter.
 */
public class StationDataSyncService extends Service {

  private StationDataSyncAdapter stationDataRetriever;

  public StationDataSyncService() {
  }

  @Override
  public synchronized void onCreate() {
    if (stationDataRetriever == null) {
      stationDataRetriever = new StationDataSyncAdapter(getApplicationContext());
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return stationDataRetriever.getSyncAdapterBinder();
  }
}
