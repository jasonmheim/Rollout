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

import android.util.Log;

import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.jasonmheim.rollout.data.StationList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Handles downloading station data. To execute on something other than the current thread, submit
 * this as a {@link Callable}.
 */
public class StationDataDownloader implements Provider<StationList>, Callable<StationList> {

  private final Gson gson;
  private final Provider<Date> dateProvider;
  private final Provider<URLConnection> urlConnectionProvider;

  @Inject
  StationDataDownloader(
      Gson gson,
      Provider<Date> dateProvider,
      @StationData Provider<URLConnection> urlConnectionProvider) {
    this.gson = gson;
    this.dateProvider = dateProvider;
    this.urlConnectionProvider = urlConnectionProvider;
  }

  @Override
  public StationList get() {
    InputStream input = null;
    try {
      input = urlConnectionProvider.get().getInputStream();
      StationList stationList = gson.fromJson(new InputStreamReader(input), StationList.class);
      Log.i("Rollout", "Providing " + stationList.stationBeanList.size() + " stations.");
      stationList.timestamp = dateProvider.get().getTime();
      return stationList;
    } catch (RuntimeException ex) {
      Log.w("Rollout", "Failed to deserialize station list data from service", ex);
      return null;
    } catch (IOException ex) {
      Log.w("Rollout", "Failed to download station list data.", ex);
      return null;
    } finally {
      Closeables.closeQuietly(input);
    }
  }

  @Override
  public StationList call() {
    return get();
  }
}
