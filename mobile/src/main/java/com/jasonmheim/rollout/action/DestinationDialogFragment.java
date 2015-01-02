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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.inject.DaggerFragment;
import com.jasonmheim.rollout.settings.Settings;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.jasonmheim.rollout.Constants.ACTION_RIDE;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_HOME;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_WORK;

/**
 * Dialog fragment to choose a destination from those that are set.
 */
public class DestinationDialogFragment extends DialogFragment
    implements DialogInterface.OnClickListener {

  @Inject
  ActionManager actionManager;

  @Inject
  Settings settings;

  public DestinationDialogFragment() {
  }

  /** Test only constructor */
  @SuppressWarnings("ValidFragment")
  DestinationDialogFragment(ActionManager actionManager, Settings settings) {
    this.actionManager = actionManager;
    this.settings = settings;
  }

  @Override
  public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        // TODO: Extract string resource
        .setTitle("Choose destination")
        .setItems(getOptions(), this);
    return builder.create();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    DaggerFragment.inject(this);
  }

  @Override
  public void onClick(DialogInterface dialogInterface, int i) {
    // HACKITY HACK HACK - this assumes the options do not change while the dialog is alive.
    // In all normal cases this should be true, but ugh, what a hack. May need to look into doing
    // some kind of list adapter thing.
    try {
      String selection = getOptions()[i];
      if (selection.equals(DESTINATION_NAME_HOME)) {
        actionManager.setAction(ACTION_RIDE, DESTINATION_NAME_HOME);
      } else if (selection.equals(DESTINATION_NAME_WORK)) {
        actionManager.setAction(ACTION_RIDE, DESTINATION_NAME_WORK);
      } else {
        actionManager.setAction(ACTION_RIDE);
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
      Log.w("Rollout", "Somehow the option indices changed between dialog open and close", ex);
      actionManager.setAction(ACTION_RIDE);
    }
  }

  // Visible for testing
  String[] getOptions() {
    List<String> options = new ArrayList<String>();
    if (settings.isHomeDestinationActive()) {
      // TODO: Extract string resource
      options.add(DESTINATION_NAME_HOME);
    }
    if (settings.isWorkDestinationActive()) {
      // TODO: Extract string resource
      options.add(DESTINATION_NAME_WORK);
    }
    // TODO: Extract string resource
    options.add("No destination, let's roam!");
    return options.toArray(new String[options.size()]);
  }
}
