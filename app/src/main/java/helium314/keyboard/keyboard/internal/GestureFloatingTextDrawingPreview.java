/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard.internal;

import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import helium314.keyboard.keyboard.KeyboardTypeface;
import helium314.keyboard.keyboard.PointerTracker;
import helium314.keyboard.latin.R;
import helium314.keyboard.latin.common.ColorType;
import helium314.keyboard.latin.common.Colors;
import helium314.keyboard.latin.common.CoordinateUtils;
import helium314.keyboard.latin.settings.Settings;

/**
 * The class for single gesture preview text. The class for multiple gesture preview text will be
 * derived from it.
 */
public class GestureFloatingTextDrawingPreview extends AbstractDrawingPreview {
    protected static final class GesturePreviewTextParams {
        public final boolean mGesturePreviewDynamic;
        public final int mGesturePreviewTextOffset;
        public final int mGesturePreviewTextHeight;
        public final float mGesturePreviewHorizontalPadding;
        public final float mGesturePreviewVerticalPadding;
        public final float mGesturePreviewRoundRadius;
        public final int mDisplayWidth;

        private final int mGesturePreviewTextSize;
        private final int mGesturePreviewTextColor;
        private final int mGesturePreviewColor;
        private final Paint mPaint = new Paint();

        private static final char[] TEXT_HEIGHT_REFERENCE_CHAR = { 'M' };

        public GesturePreviewTextParams(final TypedArray mainKeyboardViewAttr) {
            final Colors colors = Settings.getValues().mColors;
            mGesturePreviewDynamic = Settings.getValues().mGestureFloatingPreviewDynamicEnabled;
            mGesturePreviewTextSize = mainKeyboardViewAttr.getDimensionPixelSize(
                    R.styleable.MainKeyboardView_gestureFloatingPreviewTextSize, 0);
            mGesturePreviewTextColor = colors.get(ColorType.KEY_TEXT);
            mGesturePreviewTextOffset = mainKeyboardViewAttr.getDimensionPixelOffset(
                    R.styleable.MainKeyboardView_gestureFloatingPreviewTextOffset, 0);
            mGesturePreviewColor = colors.get(ColorType.GESTURE_PREVIEW);
            mGesturePreviewHorizontalPadding = mainKeyboardViewAttr.getDimension(
                    R.styleable.MainKeyboardView_gestureFloatingPreviewHorizontalPadding, 0.0f);
            mGesturePreviewVerticalPadding = mainKeyboardViewAttr.getDimension(
                    R.styleable.MainKeyboardView_gestureFloatingPreviewVerticalPadding, 0.0f);
            mGesturePreviewRoundRadius = mainKeyboardViewAttr.getDimension(
                    R.styleable.MainKeyboardView_gestureFloatingPreviewRoundRadius, 0.0f);
            mDisplayWidth = mainKeyboardViewAttr.getResources().getDisplayMetrics().widthPixels;

            final Paint textPaint = getTextPaint();
            final Rect textRect = new Rect();
            textPaint.getTextBounds(TEXT_HEIGHT_REFERENCE_CHAR, 0, 1, textRect);
            mGesturePreviewTextHeight = textRect.height();
        }

        public Paint getTextPaint() {
            mPaint.setAntiAlias(true);
            mPaint.setTextAlign(Align.CENTER);
            mPaint.setTextSize(mGesturePreviewTextSize);
            mPaint.setTypeface(KeyboardTypeface.customTypeface());
            mPaint.setColor(mGesturePreviewTextColor);
            return mPaint;
        }

        public Paint getBackgroundPaint() {
            mPaint.setColor(mGesturePreviewColor);
            return mPaint;
        }
    }

    private final GesturePreviewTextParams mParams;
    private final int[] mLastPointerCoords = CoordinateUtils.newInstance();

    public GestureFloatingTextDrawingPreview(final TypedArray mainKeyboardViewAttr) {
        mParams = new GesturePreviewTextParams(mainKeyboardViewAttr);
    }

    @Override
    public void onDeallocateMemory() {
        // Nothing to do here.
    }

    @Override
    public void setPreviewPosition(@NonNull final PointerTracker tracker) {
        if (!isPreviewEnabled()) {
            return;
        }
        tracker.getLastCoordinates(mLastPointerCoords);
        //updatePreviewPosition();
    }

}
