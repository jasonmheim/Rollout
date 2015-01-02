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

package com.jasonmheim.rollout;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jasonmheim.rollout.inject.ObjectGraphProvider;
import com.jasonmheim.rollout.module.ApplicationModule;
import com.jasonmheim.rollout.module.StationDataActivityModule;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.module.SettingsActivityModule;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Core application class. This sets up Dagger modules and kicks off the initial bindings and
 * connections.
 */
public class RolloutApplication extends Application implements ObjectGraphProvider {

  private volatile ObjectGraph graph;

  @Override public void onCreate() {
    super.onCreate();
    ensureServicesConnected();
  }

  protected List<Object> getModules() {
    return Arrays.<Object>asList(
        new ApplicationModule(this),
        new StationDataActivityModule(),
        new SettingsActivityModule()
    );
  }

  public void inject(Object object) {
    get().inject(object);
  }

  @Override
  public synchronized ObjectGraph get() {
    if (graph == null) {
      graph = ObjectGraph.create(getModules().toArray());
    }
    return graph;
  }

  private void ensureServicesConnected() {

    AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
    accountManager.addAccountExplicitly(Constants.ACCOUNT, null, Bundle.EMPTY);

    ContentResolver.setSyncAutomatically(Constants.ACCOUNT, Constants.AUTHORITY, true);
    ContentResolver.setIsSyncable(Constants.ACCOUNT, Constants.AUTHORITY, 1);

    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    // If Google Play services is available
    if (ConnectionResult.SUCCESS == resultCode) {
      Log.d("RolloutApplication", "Google Play services is available.");
    } else {
      // TODO: Handle this more gracefully - send the user to the play store.
      throw new IllegalStateException("Google Play Services is not installed: " + resultCode);
    }

    GoogleApiClient locationClient = get().get(GoogleApiClient.class);
    locationClient.connect();

    ActionManager actionManager = get().get(ActionManager.class);
    actionManager.initialize();
  }
}
