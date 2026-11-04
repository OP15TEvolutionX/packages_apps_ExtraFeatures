/*
 * SPDX-FileCopyrightText: Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.extrafeatures;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;

/** Shows The Evolver on the homepage unless it was hidden from the Extra screen's System header. */
public class TopLevelEvolverPreferenceController extends BasePreferenceController {

    static final String KEY = "evolver_hidden";

    public TopLevelEvolverPreferenceController(Context context, String key) {
        super(context, key);
    }

    static boolean isHidden(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), KEY, 0) != 0;
    }

    @Override
    public int getAvailabilityStatus() {
        return isHidden(mContext) ? CONDITIONALLY_UNAVAILABLE : AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        // The entry sits alone in its own category, which would otherwise leave a double gap.
        final Preference preference = screen.findPreference(getPreferenceKey());
        final PreferenceGroup category = preference != null ? preference.getParent() : null;
        if (category != null && category != screen) {
            category.setVisible(isAvailable());
        }
    }
}
