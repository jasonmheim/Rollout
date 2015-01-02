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

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.station.StationDataDownloader;
import com.jasonmheim.rollout.data.StationList;
import com.jasonmheim.rollout.inject.ObjectGraphProvider;
import com.squareup.otto.Bus;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A sync adapter that downloads and stores the latest station data.
 */
public class StationDataSyncAdapter extends AbstractThreadedSyncAdapter {

  @Inject
  Bus bus;

  @Inject
  Gson gson;

  @Inject
  StationDataDownloader stationListDownloader;

  @Inject
  Provider<Date> dateProvider;

  StationDataSyncAdapter(Context context) {
    super(context, false);
    Log.i("Rollout", "Creating data retriever");
    ((ObjectGraphProvider) context.getApplicationContext()).get().inject(this);
  }

  @Override
  public void onPerformSync(
      Account account,
      Bundle bundle,
      String s,
      ContentProviderClient contentProviderClient,
      SyncResult syncResult) {
    StationList stationList = stationListDownloader.get();
    if (stationList == null) {
      Log.w("Rollout", "StationList provider returned null");
    } else {
      Log.i("Rollout", "SYNCED " + stationList.stationBeanList.size() + " stations.");
      ContentValues values = new ContentValues();
      values.put("StationList", gson.toJson(stationList));
      try {
        contentProviderClient.insert(Constants.AUTHORITY_URI, values);
      } catch (RemoteException ex) {
        Log.w("Rollout", "Failed to insert sync data", ex);
      }
    }
  }
}
