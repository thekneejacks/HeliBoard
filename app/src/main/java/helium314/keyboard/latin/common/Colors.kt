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
import helium314.keyboard.latin.common.ColorType.KEY_HINT_TEXT
import helium314.keyboard.latin.common.ColorType.KEY_PREVIEW_BACKGROUND
import helium314.keyboard.latin.common.ColorType.KEY_PREVIEW_TEXT
import helium314.keyboard.latin.common.ColorType.KEY_TEXT
import helium314.keyboard.latin.common.ColorType.MAIN_BACKGROUND
import helium314.keyboard.latin.common.ColorType.POPUP_KEYS_BACKGROUND
import helium314.keyboard.latin.common.ColorType.STRIP_BACKGROUND
import helium314.keyboard.latin.common.ColorType.TOOL_BAR_KEY
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
    private val background: Int,
    private val keyText: Int,
    private val keyHintText: Int,
    private val suggestionText: Int = keyText,
) : Colors {
    private val adjustedBackground: Int
    /** further brightened or darkened variant of [adjustedBackground] */
    private val doubleAdjustedBackground: Int
    private val backgroundFilter = colorFilter(background)
    private val adjustedBackgroundFilter: ColorFilter
    private val keyTextFilter: ColorFilter

    init {

        adjustedBackground = brighten(background)
        doubleAdjustedBackground = brighten(adjustedBackground)

        val stripBackground: Int
        val pressedStripElementBackground: Int
        if (hasKeyBorders) {
            stripBackground = background
            pressedStripElementBackground = adjustedBackground
        } else {
            stripBackground = adjustedBackground
            pressedStripElementBackground = doubleAdjustedBackground
        }


        adjustedBackgroundFilter = colorFilter(adjustedBackground)
        keyTextFilter = colorFilter(keyText)
    }

    override fun get(color: ColorType): Int = when (color) {
        POPUP_KEYS_BACKGROUND, KEY_PREVIEW_BACKGROUND -> adjustedBackground
        KEY_TEXT, KEY_PREVIEW_TEXT -> keyText
        KEY_HINT_TEXT -> keyHintText
        MAIN_BACKGROUND -> background
        STRIP_BACKGROUND -> if (!hasKeyBorders) adjustedBackground else background
        TOOL_BAR_KEY -> suggestionText
    }

    override fun setColor(drawable: Drawable, color: ColorType) {
        drawable.colorFilter = getColorFilter(color)
        return
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
        KEY_TEXT -> keyTextFilter
        KEY_PREVIEW_BACKGROUND -> adjustedBackgroundFilter
        else -> colorFilter(get(color)) // create color filter (not great for performance, so the frequently used filters should be stored)
    }
}

private fun colorFilter(color: Int, mode: BlendModeCompat = BlendModeCompat.MODULATE): ColorFilter {
    // using !! for the color filter because null is only returned for unsupported blend modes, which are not used
    return BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, mode)!!
}

enum class ColorType {
    KEY_TEXT,
    KEY_HINT_TEXT,
    KEY_PREVIEW_BACKGROUND,
    KEY_PREVIEW_TEXT,
    POPUP_KEYS_BACKGROUND,
    STRIP_BACKGROUND,
    TOOL_BAR_KEY,
    MAIN_BACKGROUND,
}

// this is not used any more, but we keep in case a colorMap does not get filled for whatever reason
fun ColorType.default() = ColorUtils.setAlphaComponent(name.hashCode() and 0xffffff, 255)
