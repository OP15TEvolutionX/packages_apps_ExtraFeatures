/*
 * SPDX-FileCopyrightText: Evolution X
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.extrafeatures;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

/** A category header that reports taps, which a plain category ignores. */
public class TapCategory extends PreferenceCategory {

    private View.OnClickListener mTapListener;

    public TapCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setTapListener(View.OnClickListener listener) {
        mTapListener = listener;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        // The base bind disables the row and resets its listener, so recycled rows stay inert.
        if (mTapListener != null) {
            holder.itemView.setEnabled(true);
            holder.itemView.setOnClickListener(mTapListener);
        }
    }
}
