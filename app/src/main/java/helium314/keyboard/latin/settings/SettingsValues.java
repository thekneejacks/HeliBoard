/*
 * Copyright (C) 2011 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;

import androidx.annotation.NonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import helium314.keyboard.compat.ConfigurationCompatKt;
import helium314.keyboard.compat.IsLockedCompatKt;
import helium314.keyboard.keyboard.KeyboardActionListener;
import helium314.keyboard.keyboard.KeyboardTheme;
import helium314.keyboard.keyboard.internal.KeyboardState;
import helium314.keyboard.keyboard.internal.keyboard_parser.LocaleKeyboardInfos;
import helium314.keyboard.latin.InputAttributes;
import helium314.keyboard.latin.RichInputMethodManager;
import helium314.keyboard.latin.common.Colors;
import helium314.keyboard.latin.utils.ScriptUtils;
import helium314.keyboard.latin.utils.SubtypeSettings;
import helium314.keyboard.latin.utils.SubtypeUtilsKt;
import helium314.keyboard.latin.utils.ToolbarMode;

/**
 * When you call the constructor of this class, you may want to change the current system locale by
 * using {@link helium314.keyboard.latin.utils.RunInLocaleKt}.
 */
// Non-final for testing via mock library.
public class SettingsValues {
    // From resources:
    public final SpacingAndPunctuations mSpacingAndPunctuations;
    public final long mDoubleSpacePeriodTimeout;
    // From configuration:
    public final Locale mLocale;
    public final boolean mHasHardwareKeyboard;
    public final int mDisplayOrientation;
    // From preferences
    public final boolean mAutoCap;
    public final boolean mKeyPreviewPopupOn;
    public final boolean mLanguageSwitchKeyToOtherImes;
    public final boolean mLanguageSwitchKeyToOtherSubtypes;
    public final boolean mShowsNumberRow;
    public final boolean mShowsNumberRowInSymbols;
    public final boolean mLocalizedNumberRow;
    public final boolean mShowNumberRowHints;
    public final boolean mShowsHints;
    public final boolean mShowsPopupHints;
    public final boolean mShowTldPopupKeys;
    public final boolean mSpaceForLangChange;
    public final boolean mVarToolbarDirection;
    public final boolean mUseDoubleSpacePeriod;
    public final KeyboardActionListener.SwipeAction mSpaceSwipeHorizontal;
    public final KeyboardActionListener.SwipeAction mSpaceSwipeVertical;
    public final int mLanguageSwipeDistance;
    public final int mTouchpadSensitivity;
    public final boolean mTouchpadEdgeScroll;
    public final boolean mDeleteSwipeEnabled;
    public final boolean mAutospaceAfterPunctuation;
    public final boolean mAutospaceBeforeGestureTyping;
    public final boolean mShiftRemovesAutospace;
    public final float mKeyGapScale;
    public final String mShowMorePopupKeys;
    public final List<String> mPopupKeyOrder;
    public final List<String> mPopupKeyHintOrder;
    public final List<Locale> mSecondaryLocales;
    public final boolean mGestureFloatingPreviewTextEnabled;
    public final int mGestureFastTypingCooldown;
    public final int mGestureTrailFadeoutDuration;
    public final int mKeyLongpressTimeout;
    public final boolean mQuickPinToolbarKeys;
    public final boolean mCustomNavBarColor;
    public final float mKeyboardHeightScale;
    public final float mBottomRowScale;
    public final float mBottomPaddingScale;
    public final float mSidePaddingScale;
    public final ToolbarMode mToolbarMode;
    public final boolean mToolbarHidingGlobal;
    public final boolean mAutoShowToolbar;
    public final EnumSet<KeyboardState.Mode> mAlphaAfterSpace = EnumSet.noneOf(KeyboardState.Mode.class);
    public final boolean mRemoveRedundantPopups;
    public final float mFontSizeMultiplier;
    public final float mHintFontSizeMultiplier;
    public final float mFontSizeMultiplierEmoji;
    public final boolean mLongPressSymbolsForNumpad;

    // From the input box
    @NonNull
    public final InputAttributes mInputAttributes;

    // Deduced settings
    public final boolean mSuggestionStripHiddenPerUserSettings;
    public final boolean mIsLocked;

    // User-defined colors
    public final Colors mColors;

    // creation of Colors and SpacingAndPunctuations are the slowest parts in here, but still ok
    public SettingsValues(final Context context, final SharedPreferences prefs, final Resources res,
                          @NonNull final InputAttributes inputAttributes) {
        mLocale = ConfigurationCompatKt.locale(res.getConfiguration());
        mDisplayOrientation = res.getConfiguration().orientation;
        final InputMethodSubtype selectedSubtype = SubtypeSettings.INSTANCE.getSelectedSubtype(prefs);

        // Store the input attributes
        mInputAttributes = inputAttributes;

        // Get the settings preferences
        mIsLocked = IsLockedCompatKt.isDeviceLocked(context);
        // we want to hide the toolbar / suggestion strip entirely if device is locked
        mToolbarMode = mIsLocked ? ToolbarMode.HIDDEN : Settings.readToolbarMode(prefs);
        mToolbarHidingGlobal = mIsLocked || prefs.getBoolean(Settings.PREF_TOOLBAR_HIDING_GLOBAL, Defaults.PREF_TOOLBAR_HIDING_GLOBAL);
        mAutoCap = prefs.getBoolean(Settings.PREF_AUTO_CAP, Defaults.PREF_AUTO_CAP) && ScriptUtils.scriptSupportsUppercase(mLocale);
        mKeyPreviewPopupOn = prefs.getBoolean(Settings.PREF_POPUP_ON, Defaults.PREF_POPUP_ON);
        String languagePref = prefs.getString(Settings.PREF_LANGUAGE_SWITCH_KEY, Defaults.PREF_LANGUAGE_SWITCH_KEY);
        mLanguageSwitchKeyToOtherImes = languagePref.equals("input_method") || languagePref.equals("both");
        mLanguageSwitchKeyToOtherSubtypes = mIsLocked || languagePref.equals("internal") || languagePref.equals("both");
        mShowsNumberRow = prefs.getBoolean(Settings.PREF_SHOW_NUMBER_ROW, Defaults.PREF_SHOW_NUMBER_ROW);
        mShowsNumberRowInSymbols = prefs.getBoolean(Settings.PREF_SHOW_NUMBER_ROW_IN_SYMBOLS, Defaults.PREF_SHOW_NUMBER_ROW_IN_SYMBOLS);
        mLocalizedNumberRow = SubtypeUtilsKt.getHasLocalizedNumberRow(selectedSubtype, prefs);
        mShowNumberRowHints = prefs.getBoolean(Settings.PREF_SHOW_NUMBER_ROW_HINTS, Defaults.PREF_SHOW_NUMBER_ROW_HINTS);
        mShowsHints = prefs.getBoolean(Settings.PREF_SHOW_HINTS, Defaults.PREF_SHOW_HINTS);
        mShowsPopupHints = prefs.getBoolean(Settings.PREF_SHOW_POPUP_HINTS, Defaults.PREF_SHOW_POPUP_HINTS);
        mShowTldPopupKeys = prefs.getBoolean(Settings.PREF_SHOW_TLD_POPUP_KEYS, Defaults.PREF_SHOW_TLD_POPUP_KEYS);
        mSpaceForLangChange = prefs.getBoolean(Settings.PREF_SPACE_TO_CHANGE_LANG, Defaults.PREF_SPACE_TO_CHANGE_LANG);
        mVarToolbarDirection = mToolbarMode != ToolbarMode.HIDDEN && prefs.getBoolean(Settings.PREF_VARIABLE_TOOLBAR_DIRECTION, Defaults.PREF_VARIABLE_TOOLBAR_DIRECTION);
        mUseDoubleSpacePeriod = prefs.getBoolean(Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD, Defaults.PREF_KEY_USE_DOUBLE_SPACE_PERIOD)
                && inputAttributes.mIsGeneralTextInput;
        // confidence -> threshold and score limit are just some formulas that give something similar to the old values, so that confidence can be in a nice 0-1 range
        mDoubleSpacePeriodTimeout = 1100; // ms
        mHasHardwareKeyboard = Settings.readHasHardwareKeyboard(res.getConfiguration());
        boolean isLandscape = mDisplayOrientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean isFolded = false;
        // determine spacerWidth from display width and scale setting
        mQuickPinToolbarKeys = mToolbarMode == ToolbarMode.EXPANDABLE && prefs.getBoolean(Settings.PREF_QUICK_PIN_TOOLBAR_KEYS, Defaults.PREF_QUICK_PIN_TOOLBAR_KEYS);

        // Compute other readable settings
        mKeyLongpressTimeout = prefs.getInt(Settings.PREF_KEY_LONGPRESS_TIMEOUT, Defaults.PREF_KEY_LONGPRESS_TIMEOUT);
        mGestureFloatingPreviewTextEnabled = !mInputAttributes.mDisableGestureFloatingPreviewText
                && prefs.getBoolean(Settings.PREF_GESTURE_FLOATING_PREVIEW_TEXT, Defaults.PREF_GESTURE_FLOATING_PREVIEW_TEXT);
        mGestureFastTypingCooldown = prefs.getInt(Settings.PREF_GESTURE_FAST_TYPING_COOLDOWN, Defaults.PREF_GESTURE_FAST_TYPING_COOLDOWN);
        mGestureTrailFadeoutDuration = prefs.getInt(Settings.PREF_GESTURE_TRAIL_FADEOUT_DURATION, Defaults.PREF_GESTURE_TRAIL_FADEOUT_DURATION);
        mSuggestionStripHiddenPerUserSettings = mToolbarMode == ToolbarMode.HIDDEN || mToolbarMode == ToolbarMode.TOOLBAR_KEYS;
        mBottomRowScale = Settings.readBottomRowScale(prefs, isLandscape, isFolded);
        mSpaceSwipeHorizontal = Settings.readHorizontalSpaceSwipe(prefs);
        mSpaceSwipeVertical = Settings.readVerticalSpaceSwipe(prefs);
        mLanguageSwipeDistance = prefs.getInt(Settings.PREF_LANGUAGE_SWIPE_DISTANCE, Defaults.PREF_LANGUAGE_SWIPE_DISTANCE);
        mTouchpadSensitivity = prefs.getInt(Settings.PREF_TOUCHPAD_SENSITIVITY,
            Defaults.PREF_TOUCHPAD_SENSITIVITY);
        mTouchpadEdgeScroll = prefs.getBoolean(Settings.PREF_TOUCHPAD_EDGE_SCROLL, Defaults.PREF_TOUCHPAD_EDGE_SCROLL);
        mDeleteSwipeEnabled = prefs.getBoolean(Settings.PREF_DELETE_SWIPE, Defaults.PREF_DELETE_SWIPE);
        mAutospaceAfterPunctuation = prefs.getBoolean(Settings.PREF_AUTOSPACE_AFTER_PUNCTUATION, Defaults.PREF_AUTOSPACE_AFTER_PUNCTUATION);
        mAutospaceBeforeGestureTyping = prefs.getBoolean(Settings.PREF_AUTOSPACE_BEFORE_GESTURE_TYPING, Defaults.PREF_AUTOSPACE_BEFORE_GESTURE_TYPING);
        mShiftRemovesAutospace = prefs.getBoolean(Settings.PREF_SHIFT_REMOVES_AUTOSPACE, Defaults.PREF_SHIFT_REMOVES_AUTOSPACE);

        mKeyboardHeightScale = Settings.readHeightScale(prefs, isLandscape, isFolded);
        mSecondaryLocales = SubtypeUtilsKt.getSecondaryLocales(selectedSubtype.getExtraValue());
        mShowMorePopupKeys = SubtypeUtilsKt.getMoreKeys(selectedSubtype, prefs,
            selectedSubtype.isAsciiCapable() ? Defaults.PREF_MORE_POPUP_KEYS : LocaleKeyboardInfos.POPUP_KEYS_NORMAL);
        mColors = KeyboardTheme.getColorsForCurrentTheme(context);

        mPopupKeyOrder = SubtypeUtilsKt.getPopupKeyOrder(selectedSubtype, prefs);
        mPopupKeyHintOrder = SubtypeUtilsKt.getPopupKeyHintOrder(selectedSubtype, prefs);
        mCustomNavBarColor = prefs.getBoolean(Settings.PREF_NAVBAR_COLOR, Defaults.PREF_NAVBAR_COLOR);
        mKeyGapScale = Settings.readKeyGapScale(prefs, isLandscape, isFolded);

        mSpacingAndPunctuations = new SpacingAndPunctuations(res, false);
        mBottomPaddingScale = Settings.readBottomPaddingScale(prefs, isLandscape, isFolded);
        mSidePaddingScale = Settings.readSidePaddingScale(prefs, isLandscape, false, isFolded);
        mLongPressSymbolsForNumpad = prefs.getBoolean(Settings.PREFS_LONG_PRESS_SYMBOLS_FOR_NUMPAD, Defaults.PREFS_LONG_PRESS_SYMBOLS_FOR_NUMPAD);
        mAutoShowToolbar = mToolbarMode == ToolbarMode.EXPANDABLE && prefs.getBoolean(Settings.PREF_AUTO_SHOW_TOOLBAR, Defaults.PREF_AUTO_SHOW_TOOLBAR);
        if (prefs.getBoolean(Settings.PREF_ABC_AFTER_SYMBOL_SPACE, Defaults.PREF_ABC_AFTER_SYMBOL_SPACE)) {
            mAlphaAfterSpace.add(KeyboardState.Mode.SYMBOLS);
            mAlphaAfterSpace.add(KeyboardState.Mode.SYMBOLS_SHIFTED);
        }
        if (prefs.getBoolean(Settings.PREF_ABC_AFTER_NUMPAD_SPACE, Defaults.PREF_ABC_AFTER_NUMPAD_SPACE)) {
            mAlphaAfterSpace.add(KeyboardState.Mode.NUMPAD);
        }
        mRemoveRedundantPopups = prefs.getBoolean(Settings.PREF_REMOVE_REDUNDANT_POPUPS, Defaults.PREF_REMOVE_REDUNDANT_POPUPS);
        mFontSizeMultiplier = prefs.getFloat(Settings.PREF_FONT_SCALE, Defaults.PREF_FONT_SCALE);
        mHintFontSizeMultiplier = mShowsHints ? prefs.getFloat(Settings.PREF_HINT_FONT_SCALE, Defaults.PREF_HINT_FONT_SCALE) : 1;
        mFontSizeMultiplierEmoji = prefs.getFloat(Settings.PREF_EMOJI_FONT_SCALE, Defaults.PREF_EMOJI_FONT_SCALE);
    }

    public boolean isWordSeparator(final int code) {
        return mSpacingAndPunctuations.isWordSeparator(code);
    }

    public boolean isWordConnector(final int code) {
        return mSpacingAndPunctuations.isWordConnector(code);
    }

    public boolean isWordCodePoint(final int code) {
        return mSpacingAndPunctuations.isWordCodePoint(code);
    }

    public boolean isUsuallyPrecededBySpace(final int code) {
        return mSpacingAndPunctuations.isUsuallyPrecededBySpace(code);
    }

    public boolean isUsuallyFollowedBySpace(final int code) {
        return mSpacingAndPunctuations.isUsuallyFollowedBySpace(code);
    }

    public boolean shouldInsertSpacesAutomatically() {
        return mInputAttributes.mShouldInsertSpacesAutomatically;
    }

    public boolean isLanguageSwitchKeyEnabled() {
        final RichInputMethodManager imm = RichInputMethodManager.getInstance();
        if (!mLanguageSwitchKeyToOtherSubtypes) {
            return imm.hasMultipleEnabledIMEsOrSubtypes(false /* include aux subtypes */);
        }
        if (!mLanguageSwitchKeyToOtherImes) {
            return imm.hasMultipleEnabledSubtypesInThisIme(true /* include aux subtypes */);
        }
        return imm.hasMultipleEnabledSubtypesInThisIme(false /* include aux subtypes */)
            || imm.hasMultipleEnabledIMEsOrSubtypes(false /* include aux subtypes */);
    }

    public boolean isSameInputType(final EditorInfo editorInfo) {
        return mInputAttributes.isSameInputType(editorInfo);
    }

    public boolean hasSameOrientation(final Configuration configuration) {
        return mDisplayOrientation == configuration.orientation;
    }

    public boolean isSecondaryStripVisible() {
        return mToolbarMode != ToolbarMode.HIDDEN || !mToolbarHidingGlobal;
    }

    public String dump() {
        final StringBuilder sb = new StringBuilder("Current settings :");
        sb.append("\n   mSpacingAndPunctuations = ");
        sb.append("" + mSpacingAndPunctuations.dump());
        sb.append("\n   mAutoCap = ");
        sb.append("" + mAutoCap);
        sb.append("\n   mKeyPreviewPopupOn = ");
        sb.append("" + mKeyPreviewPopupOn);
        sb.append("\n   mLanguageSwitchKeyToOtherImes = ");
        sb.append("" + mLanguageSwitchKeyToOtherImes);
        sb.append("\n   mLanguageSwitchKeyToOtherSubtypes = ");
        sb.append("" + mLanguageSwitchKeyToOtherSubtypes);
        sb.append("\n   mUseDoubleSpacePeriod = ");
        sb.append("" + mUseDoubleSpacePeriod);
        sb.append("\n   mGestureFloatingPreviewTextEnabled = ");
        sb.append("" + mGestureFloatingPreviewTextEnabled);
        sb.append("\n   mKeyLongpressTimeout = ");
        sb.append("" + mKeyLongpressTimeout);
        sb.append("\n   mLocale = ");
        sb.append("" + mLocale);
        sb.append("\n   mInputAttributes = ");
        sb.append("" + mInputAttributes);
        sb.append("\n   mDisplayOrientation = ");
        sb.append("" + mDisplayOrientation);
        sb.append("\n   mAppWorkarounds = ");
        return sb.toString();
    }
}
