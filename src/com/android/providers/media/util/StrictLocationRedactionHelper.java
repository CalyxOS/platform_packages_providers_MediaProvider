/*
 * Copyright (C) 2024 The Calyx Institute
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

package com.android.providers.media.util;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

/** Cache the value of the STRICT_LOCATION_REDACTION setting for quick retrieval */
public class StrictLocationRedactionHelper {
    // Settings.Secure.STRICT_LOCATION_REDACTION
    private static final String STRICT_LOCATION_REDACTION = "strict_location_redaction";
    private static final int DEFAULT_VALUE = 1;

    public static StrictLocationRedactionHelper sInstance;

    private volatile Boolean mIsSettingEnabled;
    private final Context mContext;
    private final Handler mHandler;
    private final SettingObserver mSettingObserver;

    private StrictLocationRedactionHelper(final Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mSettingObserver = new SettingObserver();
        mHandler.post(() -> {
            mIsSettingEnabled = fetchCurrentSettingEnabled();
        });
    }

    public static StrictLocationRedactionHelper getInstance(final Context context) {
        synchronized (StrictLocationRedactionHelper.class) {
            if (sInstance == null) {
                sInstance = new StrictLocationRedactionHelper(context);
            }
            return sInstance;
        }
    }

    /**
     * Returns {@code true} if the strict location redaction feature is enabled (default if unset).
     */
    public boolean isSettingEnabled() {
        if (mIsSettingEnabled == null) {
            return fetchCurrentSettingEnabled();
        }
        return mIsSettingEnabled == true;
    }

    private boolean fetchCurrentSettingEnabled() {
        return Settings.Secure.getInt(mContext.getContentResolver(), STRICT_LOCATION_REDACTION,
                /* def */ DEFAULT_VALUE) != 0;
    }

    private final class SettingObserver extends ContentObserver {
        SettingObserver() {
            super(mHandler);
            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(STRICT_LOCATION_REDACTION),
                    /* notifyForDescendants */ false,
                    this);
        }

        public void onChange(boolean selfChange) {
            mIsSettingEnabled = fetchCurrentSettingEnabled();
        }
    }
}
