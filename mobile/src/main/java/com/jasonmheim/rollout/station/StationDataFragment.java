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

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jasonmheim.rollout.Constants;
import com.jasonmheim.rollout.R;
import com.jasonmheim.rollout.action.ActionManager;
import com.jasonmheim.rollout.data.StationDistance;
import com.jasonmheim.rollout.data.StationList;
import com.jasonmheim.rollout.inject.DaggerFragment;
import com.jasonmheim.rollout.sync.LastKnownStationList;
import com.jasonmheim.rollout.sync.StationDataUpdateEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Listener}
 * interface.
 */
public class StationDataFragment extends DaggerFragment
    implements AbsListView.OnItemClickListener {

  private Listener listener;
  private StationList lastStationList;

  private ListView scrollView;
  private StationDataRowViewHolder topRowViewHolder;

  @Inject
  Bus bus;

  @Inject
  LastKnownStationList lastKnownStationList;

  @Inject
  ActionManager actionManager;

  @Inject
  StationDataProcessor stationDataProcessor;

  /**
   * The Adapter which will be used to populate the ListView/GridView with
   * Views.
   */
  private ArrayAdapter<StationDistance> listAdapter;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public StationDataFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // TODO: wtf is the text parameter for?
    listAdapter = new StationDataListAdapter(getActivity(),
        android.R.id.text1,
        new ArrayList<StationDistance>(),
        actionManager);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_stationdistance, container, false);

    scrollView = (ListView) view.findViewById(R.id.scrollable_list);
    topRowViewHolder = new StationDataRowViewHolder(view.findViewById(R.id.topText));

    scrollView.setAdapter(listAdapter);

    // Set OnItemClickListener so we can be notified on item clicks
    scrollView.setOnItemClickListener(this);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    bus.register(this);
    lastStationList = lastKnownStationList.get();
    // If you don't do this, then if the activity was alive but suspended when the mode was changed
    // on the wearable, then the menu options will not appear to be correct.
    getActivity().invalidateOptionsMenu();
    refresh();
  }

  @Subscribe
  public void onStationListUpdate(final StationDataUpdateEvent event) {
    lastStationList = event.getStationList();
    refresh();
    getActivity().invalidateOptionsMenu();
  }

  void refresh() {
    if (lastStationList == null) {
      Bundle settingsBundle = new Bundle();
      settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
      settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
      ContentResolver.requestSync(Constants.ACCOUNT, Constants.AUTHORITY, settingsBundle);
      Log.w("Rollout", "Cannot refresh list; last station list not known");
      return;
    }
    if (listener == null) {
      Log.w("Rollout", "Cannot refresh list; no listener attached");
      return;
    }
    try {
      Log.i("Rollout", "Refreshing list");
      topRowViewHolder.setTopRowText(lastStationList, actionManager.getActionDisplayName());
      final List<StationDistance> results =
          stationDataProcessor.orderClosestStations(lastStationList);
      listener.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          listAdapter.clear();
          listAdapter.addAll(results);
          listAdapter.notifyDataSetChanged();
        }
      });
    } catch (Exception ex) {
      Log.e("Rollout", "Failed to submit task for execution", ex);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      listener = (Listener) activity;
      refresh();
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement StationDistanceFragment.Listener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }


  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (listener != null) {
      // DO item click stuff here
    }
  }

  /**
   * The default content for this Fragment has a TextView that is shown when
   * the list is empty. If you would like to change the text, call this method
   * to supply the text it should use.
   */
  public void setEmptyText(CharSequence emptyText) {
    View emptyView = scrollView.getEmptyView();

    if (emptyText instanceof TextView) {
      ((TextView) emptyView).setText(emptyText);
    }
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface Listener {

    public void runOnUiThread(Runnable runnable);
  }
}
