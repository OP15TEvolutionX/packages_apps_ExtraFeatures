/*
 * SPDX-FileCopyrightText: Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.extrafeatures;

import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.HapticFeedbackConstants;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;

@SearchIndexable
public class ExtraFeaturesSettings extends DashboardFragment {

    private static final String TAG = "ExtraFeaturesSettings";
    private static final int TAP_COUNT = 7;
    private static final long TAP_WINDOW_MILLIS = 3000;

    private final long[] mHits = new long[TAP_COUNT];
    private View mToolbar;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.extra_features_settings;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Seven taps on the title hide or show The Evolver on the homepage.
        mToolbar = requireActivity().findViewById(
                com.android.settingslib.collapsingtoolbar.R.id.collapsing_toolbar);
        if (mToolbar != null) {
            mToolbar.setOnClickListener(this::onTitleTap);
        }
    }

    @Override
    public void onDestroyView() {
        if (mToolbar != null) {
            mToolbar.setOnClickListener(null);
            mToolbar = null;
        }
        super.onDestroyView();
    }

    private void onTitleTap(View view) {
        System.arraycopy(mHits, 1, mHits, 0, TAP_COUNT - 1);
        mHits[TAP_COUNT - 1] = SystemClock.uptimeMillis();
        if (mHits[0] == 0 || mHits[TAP_COUNT - 1] - mHits[0] > TAP_WINDOW_MILLIS) {
            return;
        }
        Arrays.fill(mHits, 0);
        boolean hidden = !TopLevelEvolverPreferenceController.isHidden(view.getContext());
        Settings.Secure.putInt(view.getContext().getContentResolver(),
                TopLevelEvolverPreferenceController.KEY, hidden ? 1 : 0);
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.extra_features_settings);
}
