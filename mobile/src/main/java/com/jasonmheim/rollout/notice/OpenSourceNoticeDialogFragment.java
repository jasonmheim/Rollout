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

package com.jasonmheim.rollout.notice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.text.Html;

import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.inject.DaggerFragment;

import javax.inject.Inject;

import static com.jasonmheim.rollout.Constants.DISCLAIMER_VERSION;
import static com.jasonmheim.rollout.Constants.PREF_AGREED_DISCLAIMER_VERSION;

/**
 * Dialog fragment to display open source notices.
 */
public class OpenSourceNoticeDialogFragment extends DialogFragment {

  @Inject
  public OpenSourceNoticeDialogFragment() {
  }

  @Override
  public AlertDialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.open_source_notice_title)
        .setMessage(Html.fromHtml(
            getResources().getString(R.string.open_source_notice_content_html)))
        .setPositiveButton(R.string.open_source_notice_ok, new NoOpListener());
    return builder.create();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    DaggerFragment.inject(this);
  }

  static class NoOpListener implements DialogInterface.OnClickListener {

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
      // No op
    }
  }
}
