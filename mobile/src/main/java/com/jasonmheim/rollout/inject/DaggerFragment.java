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

package com.jasonmheim.rollout.inject;

import android.app.Activity;
import android.app.Fragment;

/**
 * Fragment that populates its own fields using Dagger at the attachment step. The fragment must be
 * contained within an activity whose application implements {@link ObjectGraphProvider}.
 */
public abstract class DaggerFragment extends Fragment {

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    inject(this);
  }

  /**
   * Handles injection for the given {@code fragment}. Presumes that the fragment is already
   * attached to its activity, and that this activity is part of an application that implements
   * {@link ObjectGraphProvider}. It is strongly recommended to use this only in the method
   * {@link #onAttach} after its superclass method has been invoked.
   */
  public static void inject(Fragment fragment) {
    ((ObjectGraphProvider)fragment.getActivity().getApplication()).get().inject(fragment);
  }
}
