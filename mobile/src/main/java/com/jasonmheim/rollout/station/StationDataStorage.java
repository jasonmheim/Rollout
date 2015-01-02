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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.jasonmheim.rollout.data.StationList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A reader and writer of the StationData to local disk. Synchronizes on itself, ergo this is
 * intended to be a singleton per process, and only one process - that of the content provider -
 * should interact with this class.
 */
@Singleton
class StationDataStorage {

  private final Context context;
  private final File file;
  private final Gson gson;

  @Inject
  StationDataStorage(
      Application context,
      @StationData File file,
      Gson gson) {
    this.context = context;
    this.file = file;
    this.gson = gson;
  }

  public void set(StationList stationList) {
    synchronized(file) {
      try {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        try {
          gson.toJson(stationList, writer);
        } finally {
          writer.close();
        }
      } catch (IOException ex) {
        Log.w("Rollout", "Failed to write station list data", ex);
      } catch (RuntimeException ex) {
        Log.w("Rollout", "Failed to serialize station list data", ex);
      }
    }
  }

  public StationList get() {
    synchronized(file) {
      if (!file.exists()) {
        return null;
      }
      try {
        Reader reader = new InputStreamReader(new FileInputStream(file));
        try {
          return gson.fromJson(reader, StationList.class);
        } finally {
          reader.close();
        }
      } catch (RuntimeException ex) {
        Log.w("Rollout", "Failed to deserialize station list data from file", ex);
        return null;
      } catch (IOException ex) {
        Log.w("Rollout", "Failed to read station list data", ex);
        return null;
      }
    }
  }
}
