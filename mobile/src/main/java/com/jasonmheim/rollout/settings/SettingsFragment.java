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

package com.jasonmheim.rollout.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.inject.DaggerFragment;
import com.jasonmheim.rollout.inject.ObjectGraphProvider;
import com.jasonmheim.rollout.location.LocationManager;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static android.content.Context.MODE_MULTI_PROCESS;
import static android.content.Context.MODE_PRIVATE;
import static android.text.InputType.TYPE_CLASS_NUMBER;
import static android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;
import static android.text.InputType.TYPE_NUMBER_FLAG_SIGNED;

/**
 * Fragment for managing application settings.
 */
public class SettingsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  @Inject
  LocationManager locationManager;

  @Inject
  ExecutorService executorService;

  public SettingsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO: These must match the values in ApplicationModule; make 'em common
    getPreferenceManager().setSharedPreferencesName("Rollout");
    getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE | MODE_MULTI_PROCESS);

    addPreferencesFromResource(R.xml.settings);
    setSummaryForEmptyThreshold(findPreference("pref_empty_threshold"));
    setSummaryForFullThreshold(findPreference("pref_full_threshold"));
    initializeLatitudeLongitude(findPreference("pref_destination_home_latitude"));
    initializeLatitudeLongitude(findPreference("pref_destination_home_longitude"));
    initializeLatitudeLongitude(findPreference("pref_destination_work_latitude"));
    initializeLatitudeLongitude(findPreference("pref_destination_work_longitude"));
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // Since we can't inherit from both PreferenceFragment and DaggerFragment, we have to trigger
    // the functionality of the latter here now that the activity is attached.
    DaggerFragment.inject(this);
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onResume() {
    super.onResume();
    // NOTE: Do *not* use an anonymous inner class for the listener. Preference listeners are held
    // with weak references to avoid memory leaks, so it will be garbage collected if you don't
    // handle this properly
    getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
    // TODO: notify content provider via update to SETTINGS_URI
    Preference preference = findPreference(key);
    if (key.equals("pref_empty_threshold")) {
      setSummaryForEmptyThreshold(preference);
    } else if (key.equals("pref_full_threshold")) {
      setSummaryForFullThreshold(preference);
    } else if (key.equals("pref_destination_home_set")) {
      CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
      if (checkBoxPreference.isChecked()
          && sharedPreferences.getString("pref_destination_home_latitude", "").isEmpty()
          && sharedPreferences.getString("pref_destination_home_longitude", "").isEmpty()) {
        Location lastLocation = locationManager.getLastLocation();
        if (lastLocation != null) {
          ((EditTextPreference) findPreference("pref_destination_home_latitude"))
              .setText(Double.toString(lastLocation.getLatitude()));
          ((EditTextPreference) findPreference("pref_destination_home_longitude"))
              .setText(Double.toString(lastLocation.getLongitude()));
          Toast.makeText(
              getActivity(), "Setting Home to current location.", Toast.LENGTH_SHORT).show();
        }
      }
    } else if (key.equals("pref_destination_work_set")) {
      CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
      if (checkBoxPreference.isChecked()
          && sharedPreferences.getString("pref_destination_work_latitude", "").isEmpty()
          && sharedPreferences.getString("pref_destination_work_longitude", "").isEmpty()) {
        Location lastLocation = locationManager.getLastLocation();
        if (lastLocation != null) {
          ((EditTextPreference) findPreference("pref_destination_work_latitude"))
              .setText(Double.toString(lastLocation.getLatitude()));
          ((EditTextPreference) findPreference("pref_destination_work_longitude"))
              .setText(Double.toString(lastLocation.getLongitude()));
          Toast.makeText(
              getActivity(), "Setting Work to current location.", Toast.LENGTH_SHORT).show();
        }
      }
    } else if (preference instanceof EditTextPreference) {
      setSummaryToTextValue(preference);
    }
  }

  private void setSummaryForEmptyThreshold(Preference preference) {
    EditTextPreference editTextPreference = (EditTextPreference) preference;
    String summary = "Station is \"empty\" if it has "
        + getNormalizedValue(editTextPreference)
        + " bikes.";
    editTextPreference.setSummary(summary);
  }

  private void setSummaryForFullThreshold(Preference preference) {
    EditTextPreference editTextPreference = (EditTextPreference) preference;
    String summary = "Station is \"full\" if it has "
        + getNormalizedValue(editTextPreference)
        + " docks.";
    editTextPreference.setSummary(summary);
  }

  private void initializeLatitudeLongitude(Preference preference) {
    EditTextPreference editTextPreference = (EditTextPreference) preference;
    editTextPreference.getEditText().setInputType(
        TYPE_CLASS_NUMBER | TYPE_NUMBER_FLAG_DECIMAL | TYPE_NUMBER_FLAG_SIGNED);
    setSummaryToTextValue(preference);
  }

  private void setSummaryToTextValue(Preference preference) {
    EditTextPreference editTextPreference = (EditTextPreference) preference;
    editTextPreference.setSummary(Strings.nullToEmpty(editTextPreference.getText()));
  }

  /** Return "0" if the numeric value is unset or empty, otherwise value + " or fewer" */
  private String getNormalizedValue(EditTextPreference editTextPreference) {
    String result = editTextPreference.getText();
    return result == null || result.isEmpty()  || result.equals("0") ? "0" : result + " or fewer";
  }
}
