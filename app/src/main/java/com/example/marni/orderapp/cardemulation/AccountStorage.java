/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.marni.orderapp.cardemulation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Utility class for persisting account numbers to disk.
 * <p>
 * <p>The default SharedPreferences instance is used as the backing storage. Values are cached
 * in memory for performance.
 * <p>
 * <p>This class is thread-safe.
 */
public class AccountStorage {
    private static final String PREF_ACCOUNT_NUMBER = "account_number";
    private static final String DEFAULT_ACCOUNT_NUMBER = "00000000";
    private static final String PREF_PENDING_NUMBER = "pending_number";
    private static final String DEFAULT_PENDING_NUMBER = "0";
    private static final String ERROR_CODE = "0000";
    private static final String TAG = "AccountStorage";
    private static String sAccount = null;
    private static final Object sAccountLock = new Object();

    public static void SetAccount(Context c, String s, double balance, double orderPriceTotal, int pending) {
        synchronized (sAccountLock) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            Log.i(TAG, "prefs.getString(PREF_PENDING_NUMBER, DEFAULT_PENDING_NUMBER): " + prefs.getString(PREF_PENDING_NUMBER, DEFAULT_PENDING_NUMBER));
            if (prefs.getString(PREF_PENDING_NUMBER, DEFAULT_PENDING_NUMBER).equals(DEFAULT_PENDING_NUMBER)) {
                if (balance >= orderPriceTotal) {
                    prefs.edit().putString(PREF_ACCOUNT_NUMBER, s).commit();
                    sAccount = s;
                } else {
                    //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                    prefs.edit().putString(PREF_ACCOUNT_NUMBER, DEFAULT_ACCOUNT_NUMBER).commit();
                    sAccount = DEFAULT_ACCOUNT_NUMBER;
                }
            } else {
                //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                prefs.edit().putString(PREF_ACCOUNT_NUMBER, "111").commit();
                sAccount = "111";
            }
        }
    }

    public static void ResetAccount(Context c) {
        synchronized (sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putString(PREF_ACCOUNT_NUMBER, ERROR_CODE).commit();
            sAccount = ERROR_CODE;
        }
    }

    public static String GetAccount(Context c) {
        synchronized (sAccountLock) {
            if (sAccount == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                String account = prefs.getString(PREF_ACCOUNT_NUMBER, DEFAULT_ACCOUNT_NUMBER);
                sAccount = account;
            }
            return sAccount;
        }
    }
}
