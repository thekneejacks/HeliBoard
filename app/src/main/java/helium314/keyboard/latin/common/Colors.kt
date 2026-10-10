// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin.common

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import helium314.keyboard.latin.common.ColorType.ACTION_KEY_ICON
import helium314.keyboard.latin.common.ColorType.ACTION_KEY_POPUP_KEYS_BACKGROUND
import helium314.keyboard.latin.common.ColorType.AUTOFILL_BACKGROUND_CHIP
import helium314.keyboard.latin.common.ColorType.CLIPBOARD_PIN
import helium314.keyboard.latin.common.ColorType.CLIPBOARD_SUGGESTION_BACKGROUND
import helium314.keyboard.latin.common.ColorType.CLIPBOARD_SUGGESTION_ICON
import helium314.keyboard.latin.common.ColorType.EMOJI_CATEGORY
import helium314.keyboard.latin.common.ColorType.EMOJI_CATEGORY_SELECTED
import helium314.keyboard.latin.common.ColorType.EMOJI_KEY_TEXT
import helium314.keyboard.latin.common.ColorType.EMOJI_SEARCH_BACKGROUND
import helium314.keyboard.latin.common.ColorType.EMOJI_SEARCH_TEXT
import helium314.keyboard.latin.common.ColorType.FUNCTIONAL_KEY_TEXT
import helium314.keyboard.latin.common.ColorType.GESTURE_PREVIEW
import helium314.keyboard.latin.common.ColorType.GESTURE_TRAIL
import helium314.keyboard.latin.common.ColorType.KEY_HINT_TEXT
import helium314.keyboard.latin.common.ColorType.KEY_ICON
import helium314.keyboard.latin.common.ColorType.KEY_PREVIEW_BACKGROUND
import helium314.keyboard.latin.common.ColorType.KEY_PREVIEW_TEXT
import helium314.keyboard.latin.common.ColorType.KEY_TEXT
import helium314.keyboard.latin.common.ColorType.MAIN_BACKGROUND
import helium314.keyboard.latin.common.ColorType.MORE_SUGGESTIONS_BACKGROUND
import helium314.keyboard.latin.common.ColorType.MORE_SUGGESTIONS_HINT
import helium314.keyboard.latin.common.ColorType.MORE_SUGGESTIONS_WORD_BACKGROUND
import helium314.keyboard.latin.common.ColorType.NAVIGATION_BAR
import helium314.keyboard.latin.common.ColorType.ONE_HANDED_MODE_BUTTON
import helium314.keyboard.latin.common.ColorType.POPUP_KEYS_BACKGROUND
import helium314.keyboard.latin.common.ColorType.POPUP_KEY_ICON
import helium314.keyboard.latin.common.ColorType.POPUP_KEY_TEXT
import helium314.keyboard.latin.common.ColorType.REMOVE_SUGGESTION_ICON
import helium314.keyboard.latin.common.ColorType.SHIFT_KEY_ICON
import helium314.keyboard.latin.common.ColorType.SPACE_BAR_TEXT
import helium314.keyboard.latin.common.ColorType.STRIP_BACKGROUND
import helium314.keyboard.latin.common.ColorType.SUGGESTED_WORD
import helium314.keyboard.latin.common.ColorType.SUGGESTION_AUTO_CORRECT
import helium314.keyboard.latin.common.ColorType.SUGGESTION_TYPED_WORD
import helium314.keyboard.latin.common.ColorType.SUGGESTION_VALID_WORD
import helium314.keyboard.latin.common.ColorType.TOOL_BAR_EXPAND_KEY
import helium314.keyboard.latin.common.ColorType.TOOL_BAR_EXPAND_KEY_BACKGROUND
import helium314.keyboard.latin.common.ColorType.TOOL_BAR_KEY
import helium314.keyboard.latin.common.ColorType.TOOL_BAR_KEY_ENABLED_BACKGROUND
import helium314.keyboard.latin.utils.brighten
import helium314.keyboard.latin.utils.brightenOrDarken
import helium314.keyboard.latin.utils.isBrightColor

interface Colors {
    // these theme parameters should no be in here, but are still used
    /** used in KeyboardView for label placement */

    /** used in parser to decide background of ZWNJ key */
    val hasKeyBorders: Boolean

    /** get the colorInt */
    @ColorInt fun get(color: ColorType): Int

    /** apply a color to the [drawable], may be through color filter or tint (with or without state list) */
    fun setColor(drawable: Drawable, color: ColorType)

    /** set a foreground color to the [view] */
    fun setColor(view: ImageView, color: ColorType)

    /** set a background to the [view], may replace or adjust existing background */
    fun setBackground(view: View, color: ColorType)

}

class DefaultColors (
    override val hasKeyBorders: Boolean,
    private val accent: Int,
    private val background: Int,
    private val keyBackground: Int,
    private val functionalKey: Int,
    private val spaceBar: Int,
    private val keyText: Int,
    private val keyHintText: Int,
    private val suggestionText: Int = keyText,
    private val spaceBarText: Int = keyHintText,
    private val gesture: Int = accent,
) : Colors {
    private val navBar: Int
    /** brightened or darkened variant of [background], to be used if exact background color would be
     *  bad contrast, e.g. popup keys popup or no border space bar */
    private val adjustedBackground: Int
    /** further brightened or darkened variant of [adjustedBackground] */
    private val doubleAdjustedBackground: Int
    private val adjustedSuggestionText = brightenOrDarken(suggestionText, true)

    private val backgroundFilter = colorFilter(background)
    private val adjustedBackgroundFilter: ColorFilter
    private val keyTextFilter: ColorFilter
    private val suggestionTextFilter = colorFilter(suggestionText)
    private val accentColorFilter = colorFilter(accent)

    /** color filter for the white action key icons in material theme, switches to gray if necessary for contrast */
    private val actionKeyIconColorFilter: ColorFilter?

    private val backgroundStateList: ColorStateList
    private val keyStateList: ColorStateList
    private val functionalKeyStateList: ColorStateList
    private val actionKeyStateList: ColorStateList
    private val spaceBarStateList: ColorStateList
    private val adjustedBackgroundStateList: ColorStateList
    private val stripBackgroundList: ColorStateList
    private val toolbarKeyStateList = activatedStateList(
        suggestionText,
        brighten(brighten(suggestionText))
    )

    init {

        adjustedBackground = brighten(background)
        doubleAdjustedBackground = brighten(adjustedBackground)
        adjustedBackgroundStateList = pressedStateList(doubleAdjustedBackground, adjustedBackground)

        val stripBackground: Int
        val pressedStripElementBackground: Int
        if (hasKeyBorders) {
            stripBackground = background
            pressedStripElementBackground = adjustedBackground
        } else {
            stripBackground = adjustedBackground
            pressedStripElementBackground = doubleAdjustedBackground
        }
        stripBackgroundList = pressedStateList(pressedStripElementBackground, stripBackground)
        navBar = background


        adjustedBackgroundFilter = colorFilter(adjustedBackground)
        if (hasKeyBorders) {
            backgroundStateList = pressedStateList(brightenOrDarken(background, true), background)
            keyStateList = pressedStateList(brightenOrDarken(keyBackground, true), keyBackground)
            functionalKeyStateList = pressedStateList(brightenOrDarken(functionalKey, true), functionalKey)
            actionKeyStateList = pressedStateList(brightenOrDarken(accent, true), accent)
            spaceBarStateList = pressedStateList(brightenOrDarken(spaceBar, true), spaceBar)
        } else {
            // need to set color to background if key borders are disabled, or there will be ugly keys
            backgroundStateList = pressedStateList(brightenOrDarken(background, true), background)
            keyStateList = pressedStateList(keyBackground, Color.TRANSPARENT)
            functionalKeyStateList = keyStateList
            actionKeyStateList = pressedStateList(brightenOrDarken(accent, true), accent)
            spaceBarStateList = pressedStateList(brightenOrDarken(spaceBar, true), spaceBar)
        }
        keyTextFilter = colorFilter(keyText)
        actionKeyIconColorFilter = when {
            isBrightColor(accent) -> colorFilter(Color.DKGRAY)
            else -> null
        }
    }

    override fun get(color: ColorType): Int = when (color) {
        TOOL_BAR_KEY_ENABLED_BACKGROUND, EMOJI_CATEGORY_SELECTED,
            CLIPBOARD_PIN, SHIFT_KEY_ICON -> accent
        AUTOFILL_BACKGROUND_CHIP -> if (!hasKeyBorders) background else adjustedBackground
        GESTURE_PREVIEW, POPUP_KEYS_BACKGROUND, MORE_SUGGESTIONS_BACKGROUND, KEY_PREVIEW_BACKGROUND -> adjustedBackground
        TOOL_BAR_EXPAND_KEY_BACKGROUND, CLIPBOARD_SUGGESTION_BACKGROUND -> doubleAdjustedBackground
        GESTURE_TRAIL -> gesture
        KEY_TEXT, REMOVE_SUGGESTION_ICON, FUNCTIONAL_KEY_TEXT, KEY_ICON, EMOJI_KEY_TEXT,
            POPUP_KEY_TEXT, POPUP_KEY_ICON, KEY_PREVIEW_TEXT, EMOJI_SEARCH_TEXT, CLIPBOARD_SUGGESTION_ICON -> keyText
        KEY_HINT_TEXT -> keyHintText
        SPACE_BAR_TEXT -> spaceBarText
        EMOJI_SEARCH_BACKGROUND -> functionalKey
        MORE_SUGGESTIONS_WORD_BACKGROUND, MAIN_BACKGROUND -> background
        ACTION_KEY_POPUP_KEYS_BACKGROUND -> accent
        STRIP_BACKGROUND -> if (!hasKeyBorders) adjustedBackground else background
        NAVIGATION_BAR -> navBar
        SUGGESTION_AUTO_CORRECT, EMOJI_CATEGORY, TOOL_BAR_KEY, TOOL_BAR_EXPAND_KEY, ONE_HANDED_MODE_BUTTON -> suggestionText
        MORE_SUGGESTIONS_HINT, SUGGESTED_WORD, SUGGESTION_TYPED_WORD, SUGGESTION_VALID_WORD -> adjustedSuggestionText
        ACTION_KEY_ICON -> Color.WHITE
    }

    override fun setColor(drawable: Drawable, color: ColorType) {
        val colorStateList = when (color) {
            STRIP_BACKGROUND -> stripBackgroundList
            ACTION_KEY_POPUP_KEYS_BACKGROUND -> actionKeyStateList
            TOOL_BAR_KEY -> toolbarKeyStateList
            else -> null // use color filter
        }
        if (colorStateList == null) {
            drawable.colorFilter = getColorFilter(color)
            return
        }
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.MULTIPLY)
        DrawableCompat.setTintList(drawable, colorStateList)
    }

    override fun setColor(view: ImageView, color: ColorType) {
        if (color == TOOL_BAR_KEY) {
            setColor(view.drawable, color)
            return
        }
        view.colorFilter = getColorFilter(color)
    }

    override fun setBackground(view: View, color: ColorType) {
        if (view.background == null)
            view.setBackgroundColor(Color.WHITE) // set white to make the color filters work
        when (color) {
            KEY_PREVIEW_BACKGROUND, POPUP_KEYS_BACKGROUND -> view.background.colorFilter = adjustedBackgroundFilter
            STRIP_BACKGROUND  -> setColor(view.background, color)
            MAIN_BACKGROUND -> {
                view.background.colorFilter = backgroundFilter

            }
            else -> view.background.colorFilter = backgroundFilter
        }
    }

    private fun getColorFilter(color: ColorType): ColorFilter? = when (color) {
        EMOJI_CATEGORY_SELECTED, CLIPBOARD_PIN, SHIFT_KEY_ICON -> accentColorFilter
        KEY_TEXT, KEY_ICON -> keyTextFilter
        REMOVE_SUGGESTION_ICON, EMOJI_CATEGORY, ONE_HANDED_MODE_BUTTON, TOOL_BAR_KEY, TOOL_BAR_EXPAND_KEY -> suggestionTextFilter
        KEY_PREVIEW_BACKGROUND -> adjustedBackgroundFilter
        ACTION_KEY_ICON -> actionKeyIconColorFilter
        else -> colorFilter(get(color)) // create color filter (not great for performance, so the frequently used filters should be stored)
    }
}

private fun colorFilter(color: Int, mode: BlendModeCompat = BlendModeCompat.MODULATE): ColorFilter {
    // using !! for the color filter because null is only returned for unsupported blend modes, which are not used
    return BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, mode)!!
}

private fun pressedStateList(pressed: Int, normal: Int): ColorStateList {
    val states = arrayOf(intArrayOf(android.R.attr.state_pressed), intArrayOf(-android.R.attr.state_pressed))
    return ColorStateList(states, intArrayOf(pressed, normal))
}

private fun activatedStateList(activated: Int, normal: Int): ColorStateList {
    val states = arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf(-android.R.attr.state_activated))
    return ColorStateList(states, intArrayOf(activated, normal))
}

enum class ColorType {
    ACTION_KEY_ICON,
    ACTION_KEY_POPUP_KEYS_BACKGROUND,
    AUTOFILL_BACKGROUND_CHIP,
    CLIPBOARD_PIN,
    EMOJI_CATEGORY,
    EMOJI_CATEGORY_SELECTED,
    EMOJI_KEY_TEXT,
    EMOJI_SEARCH_TEXT,
    EMOJI_SEARCH_BACKGROUND,
    FUNCTIONAL_KEY_TEXT,
    GESTURE_TRAIL,
    GESTURE_PREVIEW,
    KEY_ICON,
    KEY_TEXT,
    KEY_HINT_TEXT,
    KEY_PREVIEW_BACKGROUND,
    KEY_PREVIEW_TEXT,
    MORE_SUGGESTIONS_HINT,
    MORE_SUGGESTIONS_BACKGROUND,
    MORE_SUGGESTIONS_WORD_BACKGROUND,
    POPUP_KEYS_BACKGROUND,
    POPUP_KEY_TEXT,
    POPUP_KEY_ICON,
    NAVIGATION_BAR,
    SHIFT_KEY_ICON,
    SPACE_BAR_TEXT,
    ONE_HANDED_MODE_BUTTON,
    REMOVE_SUGGESTION_ICON,
    STRIP_BACKGROUND,
    CLIPBOARD_SUGGESTION_BACKGROUND,
    CLIPBOARD_SUGGESTION_ICON,
    SUGGESTED_WORD,
    SUGGESTION_AUTO_CORRECT,
    SUGGESTION_TYPED_WORD,
    SUGGESTION_VALID_WORD,
    TOOL_BAR_EXPAND_KEY,
    TOOL_BAR_EXPAND_KEY_BACKGROUND,
    TOOL_BAR_KEY,
    TOOL_BAR_KEY_ENABLED_BACKGROUND,
    MAIN_BACKGROUND,
}

// this is not used any more, but we keep in case a colorMap does not get filled for whatever reason
fun ColorType.default() = ColorUtils.setAlphaComponent(name.hashCode() and 0xffffff, 255)
