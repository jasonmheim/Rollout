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

package com.jasonmheim.rollout.data;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.*;

public class StationDistanceCalculatorTest {

  private static StationList stationList;
  private static Ordering<StationDistance> ordering = Ordering.natural();
  private static StationDistanceCalculator instance;

  @BeforeClass
  public static void setUpOnce() throws Exception {
    FileInputStream file = new FileInputStream("libs/test/data/sampleSnapshot.json");
    Gson gson = new Gson();
    stationList = gson.fromJson(new InputStreamReader(file), StationList.class);
    file.close();
    // This is approximately 76 Ninth Ave, New York, NY, 10011
    instance = new StationDistanceCalculator(40.7417806, -74.0045012);
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testApply() {
    Iterable<StationDistance> stationDistances =
        Iterables.transform(stationList.stationBeanList, instance);
    List<StationDistance> results = ordering.sortedCopy(stationDistances);
    for (StationDistance result : results) {
      System.out.println(result);
    }
  }
}