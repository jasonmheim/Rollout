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

package com.jasonmheim.rollout.disclaimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.*;
import android.os.Process;
import android.text.Html;
import android.util.Log;

import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.inject.DaggerFragment;
import com.jasonmheim.rollout.settings.Settings;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.jasonmheim.rollout.Constants.ACTION_RIDE;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_HOME;
import static com.jasonmheim.rollout.Constants.DESTINATION_NAME_WORK;
import static com.jasonmheim.rollout.Constants.DISCLAIMER_VERSION;
import static com.jasonmheim.rollout.Constants.PREF_AGREED_DISCLAIMER_VERSION;

/**
 * Dialog fragment to display a disclaimer to users and record their agreement. If the user declines
 * then the application will exit.
 */
public class DisclaimerDialogFragment extends DialogFragment {

  @Inject
  SharedPreferences sharedPreferences;

  @Inject
  public DisclaimerDialogFragment() {
  }

  @Override
  public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.disclaimer_title)
        .setMessage(Html.fromHtml(getResources().getString(R.string.disclaimer_content_html)))
        .setPositiveButton(R.string.disclaimer_agree, new AgreeListener())
        .setNegativeButton(R.string.disclaimer_decline, new DeclineListener());
    return builder.create();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    DaggerFragment.inject(this);
  }

  class AgreeListener implements DialogInterface.OnClickListener {

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
      sharedPreferences.edit()
          .putInt(PREF_AGREED_DISCLAIMER_VERSION, DISCLAIMER_VERSION)
          .apply();
    }
  }

  class DeclineListener implements DialogInterface.OnClickListener {

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
      // GTFO
      android.os.Process.killProcess(Process.myPid());
      System.exit(0);
    }
  }
}
