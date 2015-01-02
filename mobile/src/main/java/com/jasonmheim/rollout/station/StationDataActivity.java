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

package com.jasonmheim.rollout.station;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.action.DestinationDialogFragment;
import com.jasonmheim.rollout.disclaimer.DisclaimerDialogFragment;
import com.jasonmheim.rollout.notice.OpenSourceNoticeDialogFragment;
import com.jasonmheim.rollout.settings.SettingsActivity;
import com.jasonmheim.rollout.inject.DaggerActivity;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.settings.Settings;

import javax.inject.Inject;

import static com.jasonmheim.rollout.Constants.ACTION_SEARCH;
import static com.jasonmheim.rollout.Constants.ACTION_SILENCE;
import static com.jasonmheim.rollout.Constants.ACTION_IDLE;
import static com.jasonmheim.rollout.Constants.ACTION_RIDE;

public class StationDataActivity extends DaggerActivity
    implements StationDataFragment.Listener {

  @Inject
  ActionManager actionManager;

  @Inject
  Settings settings;

  @Inject
  public StationDataActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_station_list);
    if (!settings.isDisclaimerAgreed()) {
      getObjectGraph().get(DisclaimerDialogFragment.class)
          .show(getFragmentManager(), "DisclaimerDialog");
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    int action = actionManager.getAction();
    setItemVisibility(menu, R.id.set_action_search, action != ACTION_SEARCH);
    setItemVisibility(menu, R.id.set_action_idle, action != ACTION_IDLE);
    setItemVisibility(menu, R.id.set_action_ride, action != ACTION_RIDE);
    setItemVisibility(menu, R.id.set_action_silence, action != ACTION_SILENCE);
    return super.onPrepareOptionsMenu(menu);
  }

  private void setItemVisibility(Menu menu, int id, boolean visible) {
    MenuItem item = menu.findItem(id);
    if (item != null) {
      item.setVisible(visible);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    switch (id) {
      case R.id.set_action_search:
        actionManager.setAction(ACTION_SEARCH);
        break;
      case R.id.set_action_silence:
        actionManager.setAction(ACTION_SILENCE);
        break;
      case R.id.set_action_idle:
        actionManager.setAction(ACTION_IDLE);
        break;
      case R.id.set_action_ride:
        if (settings.isWorkDestinationActive() || settings.isHomeDestinationActive()) {
          getObjectGraph().get(DestinationDialogFragment.class)
              .show(getFragmentManager(), "DestinationDialog");
        } else {
          actionManager.setAction(ACTION_RIDE);
        }
        break;
      case R.id.menu_open_settings:
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        break;
      case R.id.menu_open_open_source_notices:
        getObjectGraph().get(OpenSourceNoticeDialogFragment.class)
            .show(getFragmentManager(), "OpenSourceNoticeDialog");
        break;

    }
    boolean result = super.onOptionsItemSelected(item);
    invalidateOptionsMenu();
    return result;
  }
}
