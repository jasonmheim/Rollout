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

package com.jasonmheim.rollout.module;

import com.jasonmheim.rollout.action.DestinationDialogFragment;
import com.jasonmheim.rollout.disclaimer.DisclaimerDialogFragment;
import com.jasonmheim.rollout.notice.OpenSourceNoticeDialogFragment;
import com.jasonmheim.rollout.station.StationDataFragment;
import com.jasonmheim.rollout.settings.SettingsFragment;

import dagger.Module;

/**
 * Created by jasonmheim on 11/15/14.
 */
@Module(
    complete = false,
    library = true,
    injects = {
        DestinationDialogFragment.class,
        DisclaimerDialogFragment.class,
        OpenSourceNoticeDialogFragment.class,
        SettingsFragment.class,
        StationDataFragment.class,
    }
)
public class FragmentModule {
}
