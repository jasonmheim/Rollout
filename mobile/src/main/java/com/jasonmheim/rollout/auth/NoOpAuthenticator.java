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

package com.jasonmheim.rollout.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * This application does not need user identity for anything. This authenticator exists only to
 * satisfy the contract of the application's Sync Adapter.
 */
public class NoOpAuthenticator extends AbstractAccountAuthenticator {

  public NoOpAuthenticator(Context context) {
    super(context);
  }

  @Override
  public Bundle editProperties(
      AccountAuthenticatorResponse accountAuthenticatorResponse,
      String s) {
    return null;
  }

  @Override
  public Bundle addAccount(
      AccountAuthenticatorResponse accountAuthenticatorResponse,
      String s,
      String s2,
      String[] strings,
      Bundle bundle) throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle confirmCredentials(
      AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account,
      Bundle bundle) throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle getAuthToken(
      AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account,
      String s,
      Bundle bundle) throws NetworkErrorException {
    return null;
  }

  @Override
  public String getAuthTokenLabel(String s) {
    return null;
  }

  @Override
  public Bundle updateCredentials(
      AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account,
      String s,
      Bundle bundle) throws NetworkErrorException {
    return null;
  }

  @Override
  public Bundle hasFeatures(
      AccountAuthenticatorResponse accountAuthenticatorResponse,
      Account account,
      String[] strings) throws NetworkErrorException {
    return null;
  }
}
