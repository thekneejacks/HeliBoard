/*
 * Copyright (C) 2011 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.WeakHashMap;

import helium314.keyboard.keyboard.internal.DrawingPreviewPlacerView;
import helium314.keyboard.keyboard.internal.DrawingProxy;
import helium314.keyboard.keyboard.internal.KeyDrawParams;
import helium314.keyboard.keyboard.internal.KeyPreviewChoreographer;
import helium314.keyboard.keyboard.internal.KeyPreviewDrawParams;
import helium314.keyboard.keyboard.internal.NonDistinctMultitouchHelper;
import helium314.keyboard.keyboard.internal.PopupKeySpec;
import helium314.keyboard.keyboard.internal.TimerHandler;
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode;
import helium314.keyboard.latin.R;
import helium314.keyboard.latin.common.ColorType;
import helium314.keyboard.latin.common.Colors;
import helium314.keyboard.latin.common.Constants;
import helium314.keyboard.latin.common.CoordinateUtils;
import helium314.keyboard.latin.settings.DebugSettings;
import helium314.keyboard.latin.settings.Defaults;
import helium314.keyboard.latin.settings.Settings;
import helium314.keyboard.latin.utils.KtxKt;

/** A view that is responsible for detecting key presses and touch movements. */
public final class MainKeyboardView extends KeyboardView implements DrawingProxy,
        PopupKeysPanel.Controller {
    private static final String TAG = MainKeyboardView.class.getSimpleName();

    /** Listener for {@link KeyboardActionListener}. */
    private KeyboardActionListener mKeyboardActionListener;

    /* Space key and its icon and background. */
    private Key mSpaceKey;
    // Stuff to draw language name on spacebar.
    private final int mLanguageOnSpacebarFinalAlpha;
    private final ObjectAnimator mLanguageOnSpacebarFadeoutAnimator;
    private boolean mHasMultipleEnabledIMEsOrSubtypes;
    private final float mLanguageOnSpacebarTextRatio;
    private float mLanguageOnSpacebarTextSize;
    private final int mLanguageOnSpacebarTextColor;
    private final float mLanguageOnSpacebarTextShadowRadius;
    private final int mLanguageOnSpacebarTextShadowColor;
    private static final float LANGUAGE_ON_SPACEBAR_TEXT_SHADOW_RADIUS_DISABLED = -1.0f;

    // Stuff to draw altCodeWhileTyping keys.
    private final ObjectAnimator mAltCodeKeyWhileTypingFadeoutAnimator;
    private final ObjectAnimator mAltCodeKeyWhileTypingFadeinAnimator;

    // Drawing preview placer view
    private final DrawingPreviewPlacerView mDrawingPreviewPlacerView;
    private final int[] mOriginCoords = CoordinateUtils.newInstance();

    // Key preview
    private final KeyPreviewDrawParams mKeyPreviewDrawParams;
    private final KeyPreviewChoreographer mKeyPreviewChoreographer;

    private final View mPopupKeysKeyboardContainer;
    private final View mPopupKeysKeyboardForActionContainer;
    private final WeakHashMap<Key, Keyboard> mPopupKeysKeyboardCache = new WeakHashMap<>();
    private final boolean mConfigShowPopupKeysKeyboardAtTouchedPoint;
    // More keys panel (used by both popup keys keyboard and more suggestions view)
    // TODO: Consider extending to support multiple popup keys panels
    private PopupKeysPanel mPopupKeysPanel;

    private final KeyDetector mKeyDetector;
    private final NonDistinctMultitouchHelper mNonDistinctMultitouchHelper;

    private final TimerHandler mTimerHandler;
    private final int mLanguageOnSpacebarHorizontalMargin;

    public MainKeyboardView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.mainKeyboardViewStyle);
    }

    public MainKeyboardView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final DrawingPreviewPlacerView drawingPreviewPlacerView =
                new DrawingPreviewPlacerView(new ContextThemeWrapper(context, R.style.platformActivityTheme), attrs);

        final TypedArray mainKeyboardViewAttr = context.obtainStyledAttributes(
                attrs, R.styleable.MainKeyboardView, defStyle, R.style.MainKeyboardView);
        final int ignoreAltCodeKeyTimeout = mainKeyboardViewAttr.getInt(
                R.styleable.MainKeyboardView_ignoreAltCodeKeyTimeout, 0);
        final int gestureRecognitionUpdateTime = mainKeyboardViewAttr.getInt(
                R.styleable.MainKeyboardView_gestureRecognitionUpdateTime, 0);
        mTimerHandler = new TimerHandler(this, ignoreAltCodeKeyTimeout, gestureRecognitionUpdateTime);

        final float keyHysteresisDistance = mainKeyboardViewAttr.getDimension(
                R.styleable.MainKeyboardView_keyHysteresisDistance, 0.0f);
        final float keyHysteresisDistanceForSlidingModifier = mainKeyboardViewAttr.getDimension(
                R.styleable.MainKeyboardView_keyHysteresisDistanceForSlidingModifier, 0.0f);
        mKeyDetector = new KeyDetector(keyHysteresisDistance, keyHysteresisDistanceForSlidingModifier);

        PointerTracker.init(mainKeyboardViewAttr, mTimerHandler, this /* DrawingProxy */);

        final SharedPreferences prefs = KtxKt.prefs(context);
        final boolean forceNonDistinctMultitouch = prefs.getBoolean(
                DebugSettings.PREF_FORCE_NON_DISTINCT_MULTITOUCH, Defaults.PREF_FORCE_NON_DISTINCT_MULTITOUCH);
        final boolean hasDistinctMultitouch = context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)
                && !forceNonDistinctMultitouch;
        mNonDistinctMultitouchHelper = hasDistinctMultitouch ? null : new NonDistinctMultitouchHelper();

        mLanguageOnSpacebarTextRatio = mainKeyboardViewAttr.getFraction(
                R.styleable.MainKeyboardView_languageOnSpacebarTextRatio, 1, 1, 1.0f)
                * Settings.getValues().mFontSizeMultiplier;
        final Colors colors = Settings.getValues().mColors;
        mLanguageOnSpacebarTextColor = colors.get(ColorType.SPACE_BAR_TEXT);
        mLanguageOnSpacebarTextShadowRadius = mainKeyboardViewAttr.getFloat(
                R.styleable.MainKeyboardView_languageOnSpacebarTextShadowRadius,
                LANGUAGE_ON_SPACEBAR_TEXT_SHADOW_RADIUS_DISABLED);
        mLanguageOnSpacebarTextShadowColor = mainKeyboardViewAttr.getColor(
                R.styleable.MainKeyboardView_languageOnSpacebarTextShadowColor, 0);
        mLanguageOnSpacebarFinalAlpha = Color.alpha(mLanguageOnSpacebarTextColor);
        final int languageOnSpacebarFadeoutAnimatorResId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_languageOnSpacebarFadeoutAnimator, 0);
        final int altCodeKeyWhileTypingFadeoutAnimatorResId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_altCodeKeyWhileTypingFadeoutAnimator, 0);
        final int altCodeKeyWhileTypingFadeinAnimatorResId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_altCodeKeyWhileTypingFadeinAnimator, 0);

        mKeyPreviewDrawParams = new KeyPreviewDrawParams(mainKeyboardViewAttr);
        mKeyPreviewChoreographer = new KeyPreviewChoreographer(mKeyPreviewDrawParams);

        final int popupKeysKeyboardLayoutId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_popupKeysKeyboardLayout, 0);
        final int popupKeysKeyboardForActionLayoutId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_popupKeysKeyboardForActionLayout,
                popupKeysKeyboardLayoutId);
        mConfigShowPopupKeysKeyboardAtTouchedPoint = mainKeyboardViewAttr.getBoolean(
                R.styleable.MainKeyboardView_showPopupKeysKeyboardAtTouchedPoint, false);


        mainKeyboardViewAttr.recycle();

        mDrawingPreviewPlacerView = drawingPreviewPlacerView;

        final LayoutInflater inflater = LayoutInflater.from(getContext());
        mPopupKeysKeyboardContainer = inflater.inflate(popupKeysKeyboardLayoutId, null);
        mPopupKeysKeyboardForActionContainer = inflater.inflate(popupKeysKeyboardForActionLayoutId, null);
        mLanguageOnSpacebarFadeoutAnimator = loadObjectAnimator(languageOnSpacebarFadeoutAnimatorResId, this);
        if (mLanguageOnSpacebarFadeoutAnimator != null)
            mLanguageOnSpacebarFadeoutAnimator.setIntValues(255, mLanguageOnSpacebarFinalAlpha);
        mAltCodeKeyWhileTypingFadeoutAnimator = loadObjectAnimator(altCodeKeyWhileTypingFadeoutAnimatorResId, this);
        mAltCodeKeyWhileTypingFadeinAnimator = loadObjectAnimator(altCodeKeyWhileTypingFadeinAnimatorResId, this);

        mKeyboardActionListener = KeyboardActionListener.EMPTY_LISTENER;

        mLanguageOnSpacebarHorizontalMargin = (int) getResources().getDimension(
                R.dimen.config_language_on_spacebar_horizontal_margin);
    }

    @Override
    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        super.setHardwareAcceleratedDrawingEnabled(enabled);
        mDrawingPreviewPlacerView.setHardwareAcceleratedDrawingEnabled(enabled);
    }

    private ObjectAnimator loadObjectAnimator(final int resId, final Object target) {
        if (resId == 0) {
            // TODO: Stop returning null.
            return null;
        }
        final ObjectAnimator animator = (ObjectAnimator)AnimatorInflater.loadAnimator(
                getContext(), resId);
        if (animator != null) {
            animator.setTarget(target);
        }
        return animator;
    }

    // Implements {@link DrawingProxy#startWhileTypingAnimation(int)}.

    public void setKeyboardActionListener(final KeyboardActionListener listener) {
        mKeyboardActionListener = listener;
        PointerTracker.setKeyboardActionListener(listener);
    }

    // TODO: We should reconsider which coordinate system should be used to represent keyboard event.
    public int getKeyX(final int x) {
        return Constants.isValidCoordinate(x) ? mKeyDetector.getTouchX(x) : x;
    }

    // TODO: We should reconsider which coordinate system should be used to represent keyboard event.
    public int getKeyY(final int y) {
        return Constants.isValidCoordinate(y) ? mKeyDetector.getTouchY(y) : y;
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     * @see Keyboard
     * @see #getKeyboard()
     * @param keyboard the keyboard to display in this view
     */
    @Override
    public void setKeyboard(@NonNull final Keyboard keyboard) {
        // Remove any pending messages, except dismissing preview and key repeat.
        mTimerHandler.cancelLongPressTimers();
        super.setKeyboard(keyboard);
        mKeyDetector.setKeyboard(
                keyboard, -getPaddingLeft(), -getPaddingTop() + getVerticalCorrection());
        PointerTracker.setKeyDetector(mKeyDetector);
        mPopupKeysKeyboardCache.clear();

        mSpaceKey = keyboard.getKey(Constants.CODE_SPACE);
        final int keyHeight = keyboard.mMostCommonKeyHeight - keyboard.mVerticalGap;
        mLanguageOnSpacebarTextSize = keyHeight * mLanguageOnSpacebarTextRatio;

        /*if (AccessibilityUtils.Companion.getInstance().isAccessibilityEnabled()) {
            if (mAccessibilityDelegate == null) {
                mAccessibilityDelegate = new MainKeyboardAccessibilityDelegate(this, mKeyDetector);
            }
            mAccessibilityDelegate.setKeyboard(keyboard);
        } else {
            mAccessibilityDelegate = null;
        }*/
    }

    /**
     * Enables or disables the key preview popup. This is a popup that shows a magnified
     * version of the depressed key. By default the preview is enabled.
     * @param previewEnabled whether or not to enable the key feedback preview
     */
    public void setKeyPreviewPopupEnabled(final boolean previewEnabled) {
        mKeyPreviewDrawParams.setPopupEnabled(previewEnabled);
    }

    private void locatePreviewPlacerView() {
        getLocationInWindow(mOriginCoords);
        mDrawingPreviewPlacerView.setKeyboardViewGeometry(mOriginCoords, getWidth(), getHeight());
    }

    private void installPreviewPlacerView() {
        final View rootView = getRootView();
        if (rootView == null) {
            //Log.w(TAG, "Cannot find root view");
            return;
        }
        ViewGroup windowContentView = rootView.findViewById(android.R.id.content);
        if (mDrawingPreviewPlacerView.getParent() instanceof ViewGroup vg)
            vg.removeView(mDrawingPreviewPlacerView); // when moving keyboard from input method content view to floating container
        // Note: It'd be very weird if we get null by android.R.id.content.
        if (windowContentView == null) {
            //Log.w(TAG, "Cannot find android.R.id.content view to add DrawingPreviewPlacerView");
            return;
        }
        windowContentView.addView(mDrawingPreviewPlacerView);
    }

    // Implements {@link DrawingProxy#onKeyPressed(Key,boolean)}.
    @Override
    public void onKeyPressed(@NonNull final Key key, final boolean withPreview) {
        key.onPressed();

        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return;
        }
        mKeyPreviewDrawParams.setVisibleOffset(-keyboard.mVerticalGap);
        if (withPreview && key.hasPreview() && mKeyPreviewDrawParams.isPopupEnabled()) {
            showKeyPreview(key);
        }
    }

    private void showKeyPreview(@NonNull final Key key) {
        locatePreviewPlacerView();
        getLocationInWindow(mOriginCoords);
        mKeyPreviewChoreographer.placeAndShowKeyPreview(key, getKeyboard().mIconsSet, getKeyDrawParams(),
                KeyboardSwitcher.getInstance().getWrapperView().getWidth(), mOriginCoords, mDrawingPreviewPlacerView);
    }

    private void dismissKeyPreviewWithoutDelay(@NonNull final Key key) {
        mKeyPreviewChoreographer.dismissKeyPreview(key);
    }

    // Implements {@link DrawingProxy#onKeyReleased(Key,boolean)}.
    @Override
    public void onKeyReleased(@NonNull final Key key, final boolean withAnimation) {
        key.onReleased();
        if (key.hasPreview()) {
            if (withAnimation) {
                dismissKeyPreview(key);
            } else {
                dismissKeyPreviewWithoutDelay(key);
            }
        }
    }

    private void dismissKeyPreview(@NonNull final Key key) {
        if (isHardwareAccelerated()) {
            mKeyPreviewChoreographer.dismissKeyPreview(key);
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        installPreviewPlacerView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDrawingPreviewPlacerView.removeAllViews();
    }

    // Implements {@link DrawingProxy@showPopupKeysKeyboard(Key,PointerTracker)}.
    @Override
    @Nullable
    public PopupKeysPanel showPopupKeysKeyboard(@NonNull final Key key,
                                                @NonNull final PointerTracker tracker) {
        final PopupKeySpec[] popupKeys = key.getPopupKeys();
        if (popupKeys == null) {
            return null;
        }
        Keyboard popupKeysKeyboard = mPopupKeysKeyboardCache.get(key);
        if (popupKeysKeyboard == null) {
            // {@link KeyPreviewDrawParams#mPreviewVisibleWidth} should have been set at
            // {@link KeyPreviewChoreographer#placeKeyPreview(Key,TextView,KeyboardIconsSet,KeyDrawParams,int,int[]},
            // though there may be some chances that the value is zero. <code>width == 0</code>
            // will cause zero-division error at
            // {@link PopupKeysKeyboardParams#setParameters(int,int,int,int,int,int,boolean,int)}.
            final boolean isSinglePopupKeyWithPreview = mKeyPreviewDrawParams.isPopupEnabled()
                    && key.hasPreview() && popupKeys.length == 1
                    && mKeyPreviewDrawParams.getVisibleWidth() > 0;
            final PopupKeysKeyboard.Builder builder = new PopupKeysKeyboard.Builder(
                    getContext(), key, getKeyboard(), isSinglePopupKeyWithPreview,
                    mKeyPreviewDrawParams.getVisibleWidth(),
                    mKeyPreviewDrawParams.getVisibleHeight(), newLabelPaint(key));
            popupKeysKeyboard = builder.build();
            mPopupKeysKeyboardCache.put(key, popupKeysKeyboard);
        }

        final View container = key.hasActionKeyPopups() ? mPopupKeysKeyboardForActionContainer
                : mPopupKeysKeyboardContainer;
        final PopupKeysKeyboardView popupKeysKeyboardView =
                container.findViewById(R.id.popup_keys_keyboard_view);
        popupKeysKeyboardView.setKeyboard(popupKeysKeyboard);
        container.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final int[] lastCoords = CoordinateUtils.newInstance();
        tracker.getLastCoordinates(lastCoords);
        final boolean keyPreviewEnabled = mKeyPreviewDrawParams.isPopupEnabled() && key.hasPreview();
        // The popup keys keyboard is usually horizontally aligned with the center of the parent key.
        // If showPopupKeysKeyboardAtTouchedPoint is true and the key preview is disabled, the more
        // keys keyboard is placed at the touch point of the parent key.
        final int pointX = (mConfigShowPopupKeysKeyboardAtTouchedPoint && !keyPreviewEnabled)
                ? CoordinateUtils.x(lastCoords)
                : key.getX() + key.getWidth() / 2;
        // The popup keys keyboard is usually vertically aligned with the top edge of the parent key
        // (plus vertical gap). If the key preview is enabled, the popup keys keyboard is vertically
        // aligned with the bottom edge of the visible part of the key preview.
        // {@code mPreviewVisibleOffset} has been set appropriately in
        // {@link KeyboardView#showKeyPreview(PointerTracker)}.
        final int pointY = key.getY() + mKeyPreviewDrawParams.getVisibleOffset();
        popupKeysKeyboardView.showPopupKeysPanel(this, this, pointX, pointY, mKeyboardActionListener);
        return popupKeysKeyboardView;
    }

    @Override
    public void onShowPopupKeysPanel(final PopupKeysPanel panel) {
        locatePreviewPlacerView();
        // Dismiss another {@link PopupKeysPanel} that may be being showed.
        onDismissPopupKeysPanel();
        // Dismiss all key previews that may be being showed.
        PointerTracker.setReleasedKeyGraphicsToAllKeys();
        panel.showInParent(mDrawingPreviewPlacerView);
        mPopupKeysPanel = panel;
    }

    public boolean isShowingPopupKeysPanel() {
        return mPopupKeysPanel != null && mPopupKeysPanel.isShowingInParent();
    }

    @Override
    public void onCancelPopupKeysPanel() {
        PointerTracker.dismissAllPopupKeysPanels();
    }

    @Override
    public void onDismissPopupKeysPanel() {
        if (isShowingPopupKeysPanel()) {
            mPopupKeysPanel.removeFromParent();
            mPopupKeysPanel = null;
        }
    }

    public void startDoubleTapShiftKeyTimer() {
        mTimerHandler.startDoubleTapShiftKeyTimer();
    }

    public void cancelDoubleTapShiftKeyTimer() {
        mTimerHandler.cancelDoubleTapShiftKeyTimer();
    }

    public boolean popDoubleTapShiftKeyTimer() {
        return mTimerHandler.popDoubleTapShiftKeyTimer();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (getKeyboard() == null) {
            return false;
        }
        if (mNonDistinctMultitouchHelper != null) {
            if (event.getPointerCount() > 1 && mTimerHandler.isInKeyRepeat()) {
                // Key repeating timer will be canceled if 2 or popup keys are in action.
                mTimerHandler.cancelKeyRepeatTimers();
            }
            // Non distinct multitouch screen support
            mNonDistinctMultitouchHelper.processMotionEvent(event, mKeyDetector);
            return true;
        }
        return processMotionEvent(event);
    }

    public boolean processMotionEvent(final MotionEvent event) {
        final int index = event.getActionIndex();
        final int id = event.getPointerId(index);
        final PointerTracker tracker = PointerTracker.getPointerTracker(id);
        // When a popup keys panel is showing, we should ignore other fingers' single touch events
        // other than the finger that is showing the popup keys panel.
        if (isShowingPopupKeysPanel() && !tracker.isShowingPopupKeysPanel()
                && PointerTracker.getActivePointerTrackerCount() == 1) {
            return true;
        }
        tracker.processMotionEvent(event, mKeyDetector);
        return true;
    }

    public void cancelAllOngoingEvents() {
        mTimerHandler.cancelAllMessages();
        PointerTracker.setReleasedKeyGraphicsToAllKeys();
        PointerTracker.dismissAllPopupKeysPanels();
        PointerTracker.cancelAllPointerTrackers();
    }

    public void closing() {
        cancelAllOngoingEvents();
        mPopupKeysKeyboardCache.clear();
    }

    public void onHideWindow() {
        onDismissPopupKeysPanel();
        //final MainKeyboardAccessibilityDelegate accessibilityDelegate = mAccessibilityDelegate;
        /*if (accessibilityDelegate != null
                && AccessibilityUtils.Companion.getInstance().isAccessibilityEnabled()) {
            accessibilityDelegate.onHideWindow();
        }*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onHoverEvent(final MotionEvent event) {
        /*final MainKeyboardAccessibilityDelegate accessibilityDelegate = mAccessibilityDelegate;
        if (accessibilityDelegate != null
                && AccessibilityUtils.Companion.getInstance().isTouchExplorationEnabled()) {
            return accessibilityDelegate.onHoverEvent(event);
        }*/
        return super.onHoverEvent(event);
    }

    public void updateShortcutKey(final boolean available) {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return;
        }
        final Key shortcutKey = keyboard.getKey(KeyCode.VOICE_INPUT);
        if (shortcutKey == null) {
            return;
        }
        shortcutKey.setEnabled(available);
    }

    public void updateLockState(final int keyCode, final boolean locked) {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return;
        }
        final Key lockKey = keyboard.getKey(keyCode);
        if (lockKey == null) {
            return;
        }
        lockKey.setLocked(locked);
    }

    @Override
    protected void onDrawKeyTopVisuals(@NonNull final Key key, @NonNull final Canvas canvas,
            @NonNull final Paint paint, @NonNull final KeyDrawParams params) {
        super.onDrawKeyTopVisuals(key, canvas, paint, params);
        final int code = key.getCode();
        if (code == Constants.CODE_SPACE) {
            // Whether space key needs to show the "..." popup hint for special purposes
            if (key.isLongPressEnabled() && mHasMultipleEnabledIMEsOrSubtypes && Settings.getValues().mSpaceForLangChange) {
                drawKeyPopupHint(key, canvas, paint, params);
            }
        } else if (code == KeyCode.LANGUAGE_SWITCH) {
            drawKeyPopupHint(key, canvas, paint, params);
        }
    }

    @Override
    public void deallocateMemory() {
        super.deallocateMemory();
        mDrawingPreviewPlacerView.deallocateMemory();
    }
}
