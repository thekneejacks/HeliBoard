/*
 * Copyright (C) 2008 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import helium314.keyboard.event.Event;
import helium314.keyboard.keyboard.internal.KeyboardState;
import helium314.keyboard.keyboard.internal.LayoutDirective;
import helium314.keyboard.keyboard.internal.ShiftMode;
import helium314.keyboard.latin.CapsMode;
import helium314.keyboard.latin.InputView;
import helium314.keyboard.latin.KeyboardWrapperView;
import helium314.keyboard.latin.LatinIME;
import helium314.keyboard.latin.R;
import helium314.keyboard.latin.RichInputMethodManager;
import helium314.keyboard.latin.RichInputMethodSubtype;
import helium314.keyboard.latin.settings.Settings;
import helium314.keyboard.latin.settings.SettingsValues;
import helium314.keyboard.latin.suggestions.SuggestionStripView;
import helium314.keyboard.latin.utils.KtxKt;
import helium314.keyboard.latin.utils.RecapitalizeMode;
import helium314.keyboard.latin.utils.ResourceUtils;
import helium314.keyboard.latin.utils.ScriptUtils;
import helium314.keyboard.latin.utils.SubtypeUtilsAdditional;

public final class KeyboardSwitcher {
    private static final String TAG = KeyboardSwitcher.class.getSimpleName();

    private InputView mCurrentInputView;
    private KeyboardWrapperView mKeyboardViewWrapper;
    private View mMainKeyboardFrame;
    private MainKeyboardView mKeyboardView;
    private SuggestionStripView mSuggestionStripView;
    private FrameLayout mStripContainer;
    private LatinIME mLatinIME;
    private RichInputMethodManager mRichImm;

    private KeyboardState mState;

    private KeyboardLayoutSet mKeyboardLayoutSet;

    private KeyboardTheme mKeyboardTheme;
    private Context mThemeContext;
    private int mCurrentUiMode;
    private int mCurrentOrientation;
    private int mCurrentDpi;
    private boolean mThemeNeedsReload;

    @SuppressLint("StaticFieldLeak") // this is a keyboard, we want to keep it alive in background
    private static final KeyboardSwitcher sInstance = new KeyboardSwitcher();

    public static KeyboardSwitcher getInstance() {
        return sInstance;
    }

    private KeyboardSwitcher() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final LatinIME latinIme) {
        sInstance.initInternal(latinIme);
    }

    private void initInternal(final LatinIME latinIme) {
        mLatinIME = latinIme;
        mRichImm = RichInputMethodManager.getInstance();
        mState = new KeyboardState(new SwitchActions());
    }

    private void updateKeyboardThemeAndContextThemeWrapper(final Context context, final KeyboardTheme keyboardTheme) {
        final Resources res = context.getResources();
        if (mThemeNeedsReload
                || mThemeContext == null
                || !keyboardTheme.equals(mKeyboardTheme)
                || mCurrentDpi != res.getDisplayMetrics().densityDpi
                || mCurrentOrientation != res.getConfiguration().orientation
                || (mCurrentUiMode & Configuration.UI_MODE_NIGHT_MASK) != (res.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                || !mThemeContext.getResources().equals(res) ){
            mThemeNeedsReload = false;
            mKeyboardTheme = keyboardTheme;
            mThemeContext = new ContextThemeWrapper(context, keyboardTheme.mStyleId);
            mCurrentUiMode = res.getConfiguration().uiMode;
            mCurrentOrientation = res.getConfiguration().orientation;
            mCurrentDpi = res.getDisplayMetrics().densityDpi;
            KeyboardLayoutSet.Companion.onKeyboardThemeChanged();
        }
    }

    public void loadKeyboard(final EditorInfo editorInfo, final SettingsValues settingsValues,
            final int currentAutoCapsState, @Nullable final RecapitalizeMode currentRecapitalizeState,
            KeyboardLayoutSet.InternalAction internalAction) {
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(
                mThemeContext, editorInfo);
        final int keyboardWidth = ResourceUtils.getKeyboardWidth(mThemeContext, settingsValues);
        final int keyboardHeight = ResourceUtils.getKeyboardHeight(mThemeContext.getResources(), settingsValues);
        mKeyboardLayoutSet = builder.setKeyboardGeometry(keyboardWidth, keyboardHeight)
                .setSubtype(mRichImm.getCurrentSubtype())
                .setNumberRowEnabled(settingsValues.mShowsNumberRow)
                .setNumberRowInSymbolsEnabled(settingsValues.mShowsNumberRowInSymbols)
                .setLanguageSwitchKeyEnabled(settingsValues.isLanguageSwitchKeyEnabled())
                .setInternalAction(internalAction)
                .build();
        try {
            mState.onLoadKeyboard(currentAutoCapsState, currentRecapitalizeState);
        } catch (KeyboardLayoutSet.Companion.KeyboardLayoutSetException e) {
            //Log.e(TAG, "loading keyboard failed: " + e.getKeyboardId(), e.getCause());
            try {
                final InputMethodSubtype defaults = SubtypeUtilsAdditional.INSTANCE.createDefaultSubtype(mRichImm.getCurrentSubtypeLocale());
                mKeyboardLayoutSet = builder.setKeyboardGeometry(keyboardWidth, keyboardHeight)
                        .setSubtype(RichInputMethodSubtype.Companion.get(defaults))
                        .setNumberRowEnabled(settingsValues.mShowsNumberRow)
                        .setNumberRowInSymbolsEnabled(settingsValues.mShowsNumberRowInSymbols)
                        .setLanguageSwitchKeyEnabled(settingsValues.isLanguageSwitchKeyEnabled())
                        .build();
                mState.onLoadKeyboard(currentAutoCapsState, currentRecapitalizeState);
                showToast("error loading the keyboard, falling back to defaults", false);
            } catch (KeyboardLayoutSet.Companion.KeyboardLayoutSetException e2) {
                //Log.e(TAG, "even fallback to defaults failed: " + e2.getKeyboardId(), e2.getCause());
            }
        }
    }

    public void saveKeyboardState() {
        if (getKeyboard() != null || isShowingEmojiPalettes() || isShowingClipboardHistory()) {
            mState.onSaveKeyboardState();
        }
    }

    public void onHideWindow() {
        if (mKeyboardView != null) {
            mKeyboardView.onHideWindow();
        }
    }

    @Nullable public Keyboard getKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.getKeyboard();
        }
        return null;
    }

    // TODO: Remove this method. Come up with a more comprehensive way to reset the keyboard layout
    // when a keyboard layout set doesn't get reloaded in LatinIME.onStartInputViewInternal().
    public void resetKeyboardStateToAlphabet() {
        mState.onResetKeyboardStateToAlphabet(mLatinIME.getCurrentAutoCapsState(), mLatinIME.getCurrentRecapitalizeState());
    }

    public void onPressKey(int code, int pointerCount, int currentAutoCapsState,
            @Nullable RecapitalizeMode currentRecapitalizeState) {
        mState.onPressKey(code, pointerCount, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onReleaseKey(final int code, final boolean withSliding,
            final int currentAutoCapsState, @Nullable final RecapitalizeMode currentRecapitalizeState) {
        mState.onReleaseKey(code, withSliding, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onFinishSlidingInput(final int currentAutoCapsState,
            @Nullable final RecapitalizeMode currentRecapitalizeState) {
        mState.onFinishSlidingInput(currentAutoCapsState, currentRecapitalizeState);
    }

    public boolean isImeSuppressedByHardwareKeyboard(
            @NonNull final SettingsValues settingsValues,
            @NonNull final KeyboardSwitchState toggleState) {
        return settingsValues.mHasHardwareKeyboard && toggleState == KeyboardSwitchState.HIDDEN;
    }

    private void setMainKeyboardFrame(
            @NonNull final SettingsValues settingsValues,
            @NonNull final KeyboardSwitchState toggleState) {
        final int visibility = isImeSuppressedByHardwareKeyboard(settingsValues, toggleState) ? View.GONE : View.VISIBLE;
        final int stripVisibility = mLatinIME.hasSuggestionStripView()? View.VISIBLE : View.GONE;
        mStripContainer.setVisibility(stripVisibility);
        PointerTracker.switchTo(mKeyboardView);
        mKeyboardView.setVisibility(visibility);
        // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
        // @see #getVisibleKeyboardView() and
        // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)
        mMainKeyboardFrame.setVisibility(visibility);
        mKeyboardViewWrapper.setVisibility(Settings.getInstance().readShowToolbarOnly() ? View.GONE : View.VISIBLE);
        mSuggestionStripView.setVisibility(stripVisibility);
    }

    public void toggleLayout(@NonNull LayoutDirective.Utility layout, int autoCapsFlags, @Nullable RecapitalizeMode recapitalizeMode) {
        mState.toggleLayout(layout, autoCapsFlags, recapitalizeMode);
    }

    public void onLongPressAlphaSymbolForNumpad() {
        if (SwitchActions.DEBUG_ACTION) {
            ////Log.(TAG, "onLongPressAlphaSymbol");
        }
        mState.onLongPressAlphaSymbolForNumpad();
    }

    public enum KeyboardSwitchState {
        HIDDEN(null),
        SYMBOLS_SHIFTED(KeyboardElement.SYMBOLS_SHIFTED),
        OTHER(null);

        @Nullable final KeyboardElement mKeyboardElement;

        KeyboardSwitchState(@Nullable KeyboardElement keyboardElement) {
            mKeyboardElement = keyboardElement;
        }
    }

    public KeyboardSwitchState getKeyboardSwitchState() {
        boolean hidden = !isShowingEmojiPalettes() && !isShowingClipboardHistory()
                && (mKeyboardLayoutSet == null
                || mKeyboardView == null
                || !mKeyboardView.isShown());
        if (hidden) {
            return KeyboardSwitchState.HIDDEN;
        } else if (isShowingKeyboardId(KeyboardElement.SYMBOLS_SHIFTED)) {
            return KeyboardSwitchState.SYMBOLS_SHIFTED;
        }
        return KeyboardSwitchState.OTHER;
    }

    public void updateShiftState(final int autoCapsFlags, @Nullable final RecapitalizeMode recapitalizeMode) {
        if (SwitchActions.DEBUG_ACTION) {
            ////Log.(TAG, "updateShiftState: " + " autoCapsFlags=" + CapsModeUtils.flagsToString(autoCapsFlags) + " recapitalizeMode=" + recapitalizeMode);
        }
        mState.onUpdateShiftState(autoCapsFlags, recapitalizeMode);
    }

    public void reloadMainKeyboard() {
        // Reload the entire keyboard, and switch to the previous layout
        loadKeyboard(mLatinIME.getCurrentInputEditorInfo(), Settings.getValues(),
                mLatinIME.getCurrentAutoCapsState(), mLatinIME.getCurrentRecapitalizeState(), null);
    }

    /**
     * Displays a toast message.
     *
     * @param text The text to display in the toast message.
     * @param briefToast If true, the toast duration will be short; otherwise, it will last longer.
     */
    public void showToast(final String text, final boolean briefToast){
        final int toastLength = briefToast ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
        final Toast toast = Toast.makeText(mLatinIME, text, toastLength);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Updates state machine to figure out when to automatically switch back to the previous mode.
     */
    public void onEvent(final Event event, final int currentAutoCapsState,
            @Nullable final RecapitalizeMode currentRecapitalizeState) {
        mState.onEvent(event, currentAutoCapsState, currentRecapitalizeState);
    }

    public boolean isShowingKeyboardId(@NonNull KeyboardElement... keyboardElements) {
        if (mKeyboardView == null || !mKeyboardView.isShown()) {
            return false;
        }
        final Keyboard keyboard = mKeyboardView.getKeyboard();
        if (keyboard == null) // may happen when using hardware keyboard
            return false;
        KeyboardElement activeKeyboardId = keyboard.mId.getElement();
        for (KeyboardElement keyboardElement : keyboardElements) {
            if (activeKeyboardId == keyboardElement) {
                return true;
            }
        }
        return false;
    }

    public boolean isShowingEmojiPalettes() {
        return false;
    }

    public boolean isShowingClipboardHistory() {
        return false;
    }

    public boolean isShowingPopupKeysPanel() {
        if (isShowingEmojiPalettes() || isShowingClipboardHistory()) {
            return false;
        }
        return mKeyboardView.isShowingPopupKeysPanel();
    }

    public boolean isShowingStripContainer() {
        return mStripContainer.isShown();
    }

    public View getVisibleKeyboardView() {
        return mKeyboardView;
    }

    public View getWrapperView() {
        return mKeyboardViewWrapper;
    }


    public MainKeyboardView getMainKeyboardView() {
        return mKeyboardView;
    }

    public FrameLayout getStripContainer() { return mStripContainer; }

    public void deallocateMemory() {
        if (mKeyboardView != null) {
            mKeyboardView.cancelAllOngoingEvents();
            mKeyboardView.deallocateMemory();
        }
    }

    public void trimMemory() {
    }

    @SuppressLint("InflateParams")
    public View onCreateInputView(@NonNull Context displayContext, boolean isHardwareAcceleratedDrawingEnabled) {
        ////Log.(TAG, "create new input view");
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }
        PointerTracker.clearOldViewData();
        SharedPreferences prefs = KtxKt.prefs(displayContext);
        if (mSuggestionStripView != null)
            prefs.unregisterOnSharedPreferenceChangeListener(mSuggestionStripView);
        if (mThemeNeedsReload) // necessary in some cases (e.g. theme switch) when mThemeNeedsReload is set before first keyboard load
            Settings.getInstance().loadSettings(displayContext, Settings.getValues().mLocale, Settings.getValues().mInputAttributes);

        updateKeyboardThemeAndContextThemeWrapper(displayContext, KeyboardTheme.getKeyboardTheme());
        mCurrentInputView = (InputView)LayoutInflater.from(mThemeContext).inflate(R.layout.input_view, null);
        mMainKeyboardFrame = mCurrentInputView.findViewById(R.id.main_keyboard_frame);

        mKeyboardViewWrapper = mCurrentInputView.findViewById(R.id.keyboard_view_wrapper);
        mKeyboardViewWrapper.setKeyboardActionListener(mLatinIME.mKeyboardActionListener);
        mKeyboardView = mCurrentInputView.findViewById(R.id.keyboard_view);
        mKeyboardView.setHardwareAcceleratedDrawingEnabled(isHardwareAcceleratedDrawingEnabled);
        mKeyboardView.setKeyboardActionListener(mLatinIME.mKeyboardActionListener);
        mSuggestionStripView = mCurrentInputView.findViewById(R.id.suggestion_strip_view);
        mStripContainer = mCurrentInputView.findViewById(R.id.strip_container);

        prefs.registerOnSharedPreferenceChangeListener(mSuggestionStripView);
        PointerTracker.switchTo(mKeyboardView);
        return mCurrentInputView;
    }

    public CapsMode getKeyboardCapsMode() {
        Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return CapsMode.OFF;
        }
        return keyboard.mId.getElement().getCapsMode();
    }

    public String getCurrentKeyboardScript() {
        if (null == mKeyboardLayoutSet) {
            return ScriptUtils.SCRIPT_UNKNOWN;
        }
        return mKeyboardLayoutSet.getScript();
    }

    public void switchToSubtype(InputMethodSubtype subtype) {
        mLatinIME.switchToSubtype(subtype);
    }


    // private SwitchActions implementation so e.g. setEmojiKeyboard can only be called via KeyboardState (avoid inconsistencies!)
    private class SwitchActions implements KeyboardState.SwitchActions {
        @Override
        public void setAlphabetKeyboard(@NonNull ShiftMode shiftMode) {
            if (DEBUG_ACTION) {
                ////Log.(TAG, "setAlphabetKeyboard");
            }
            setKeyboard(shiftMode.element, KeyboardSwitchState.OTHER);
        }

        @Override
        public void setSymbolsKeyboard() {
            if (DEBUG_ACTION) {
                ////Log.(TAG, "setSymbolsKeyboard");
            }
            setKeyboard(KeyboardElement.SYMBOLS, KeyboardSwitchState.OTHER);
        }

        @Override
        public void setSymbolsShiftedKeyboard() {
            if (DEBUG_ACTION) {
                ////Log.(TAG, "setSymbolsShiftedKeyboard");
            }
            setKeyboard(KeyboardElement.SYMBOLS_SHIFTED, KeyboardSwitchState.SYMBOLS_SHIFTED);
        }

        @Override
        public void setNumpadKeyboard() {
            if (DEBUG_ACTION) {
                ////Log.(TAG, "setNumpadKeyboard");
            }
            setKeyboard(KeyboardElement.NUMPAD, KeyboardSwitchState.OTHER);
        }

        @Override
        public void startDoubleTapShiftKeyTimer() {
            if (DEBUG_TIMER_ACTION) {
                ////Log.(TAG, "startDoubleTapShiftKeyTimer");
            }
            MainKeyboardView keyboardView = getMainKeyboardView();
            if (keyboardView != null) {
                keyboardView.startDoubleTapShiftKeyTimer();
            }
        }

        @Override
        public void cancelDoubleTapShiftKeyTimer() {
            if (DEBUG_TIMER_ACTION) {
                ////Log.(TAG, "cancelDoubleTapShiftKeyTimer");
            }
            MainKeyboardView keyboardView = getMainKeyboardView();
            if (keyboardView != null) {
                keyboardView.cancelDoubleTapShiftKeyTimer();
            }
        }


        @Override
        public boolean popDoubleTapShiftKeyTimer() {
            if (DEBUG_TIMER_ACTION) {
                ////Log.(TAG, "isInDoubleTapShiftKeyTimeout");
            }
            MainKeyboardView keyboardView = getMainKeyboardView();
            return keyboardView != null && keyboardView.popDoubleTapShiftKeyTimer();
        }

        // not a SwitchAction, but should only be called from a SwitchAction to avoid inconsistent state / actual layout
        private void setKeyboard(KeyboardElement keyboardElement, @NonNull KeyboardSwitchState toggleState) {
            // with a hardware keyboard we might get here without ever calling onCreateInputView, so don't crash
            if (mKeyboardView == null) return;

            // Make {@link MainKeyboardView} visible and hide {@link EmojiPalettesView}.
            SettingsValues currentSettingsValues = Settings.getValues();
            setMainKeyboardFrame(currentSettingsValues, toggleState);
            // TODO: pass this object to setKeyboard instead of getting the current values.
            MainKeyboardView keyboardView = mKeyboardView;
            Keyboard newKeyboard = mKeyboardLayoutSet.getKeyboard(keyboardElement);
            keyboardView.setKeyboard(newKeyboard);
            mCurrentInputView.setKeyboardTopPadding(newKeyboard.mTopPadding);
            keyboardView.setKeyPreviewPopupEnabled(currentSettingsValues.mKeyPreviewPopupOn);
            keyboardView.updateShortcutKey(mRichImm.isShortcutImeReady());


        }
    }
}
