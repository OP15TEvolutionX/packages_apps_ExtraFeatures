/*
 * SPDX-FileCopyrightText: Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.extrafeatures;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.android.settings.core.BasePreferenceController;

/** Hides the Smartspacer entry while the app is missing or disabled. */
public class SmartspacerPreferenceController extends BasePreferenceController {

    private static final String PACKAGE = "com.kieronquinn.app.smartspacer";

    public SmartspacerPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(PACKAGE, 0);
            return info.enabled ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
        } catch (PackageManager.NameNotFoundException e) {
            return UNSUPPORTED_ON_DEVICE;
        }
    }
}
