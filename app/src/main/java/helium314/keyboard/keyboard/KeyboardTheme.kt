/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */
package helium314.keyboard.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.Colors
import helium314.keyboard.latin.common.DefaultColors
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.brightenOrDarken
import helium314.keyboard.latin.utils.isBrightColor
import helium314.keyboard.latin.utils.isGoodContrast
import helium314.keyboard.latin.utils.prefs
import kotlinx.serialization.Serializable

class KeyboardTheme // Note: The themeId should be aligned with "themeId" attribute of Keyboard style in values/themes-<style>.xml.
private constructor(val themeId: Int, @JvmField val mStyleId: Int) {
    override fun equals(other: Any?) = if (other === this) true
        else (other as? KeyboardTheme)?.themeId == themeId

    override fun hashCode(): Int {
        return themeId
    }

    companion object {

        // named colors, with names from old settings
        const val COLOR_ACCENT = "accent"
        const val COLOR_GESTURE = "gesture"
        const val COLOR_SUGGESTION_TEXT = "suggestion_text"
        const val COLOR_TEXT = "text"
        const val COLOR_HINT_TEXT = "hint_text"
        const val COLOR_KEYS = "keys"
        const val COLOR_FUNCTIONAL_KEYS = "functional_keys"
        const val COLOR_SPACEBAR = "spacebar"
        const val COLOR_SPACEBAR_TEXT = "spacebar_text"
        const val COLOR_BACKGROUND = "background"



        private const val THEME_ID_LXX_BASE = 1
        
        @JvmStatic
        fun getKeyboardTheme(): KeyboardTheme {
            return KeyboardTheme(THEME_ID_LXX_BASE, R.style.KeyboardTheme_LXX_Base)
        }


        fun getThemeActionAndEmojiKeyLabelFlags(themeId: Int): Int {
            return Key.LABEL_FLAGS_KEEP_BACKGROUND_ASPECT_RATIO
        }

        @JvmStatic
        fun getColorsForCurrentTheme(context: Context): Colors {
            val prefs = context.prefs()
            val isNight = true

            return getThemeColors(context, prefs, isNight)
        }

        private fun getThemeColors(context: Context, prefs: SharedPreferences, isNight: Boolean): Colors {
            val hasBorders = prefs.getBoolean(Settings.PREF_THEME_KEY_BORDERS, Defaults.PREF_THEME_KEY_BORDERS)
            val backgroundImage = Settings.readUserBackgroundImage(context, isNight)
            //use black theme only
            return DefaultColors(
                    hasBorders,
                    ContextCompat.getColor(context, R.color.gesture_trail_color_lxx_dark),
                    ContextCompat.getColor(context, R.color.background_amoled_black),
                    ContextCompat.getColor(context, R.color.background_amoled_dark),
                    ContextCompat.getColor(context, R.color.background_amoled_dark),
                    ContextCompat.getColor(context, R.color.background_amoled_dark),
                    ContextCompat.getColor(context, R.color.key_text_color_lxx_dark),
                    ContextCompat.getColor(context, R.color.key_hint_letter_color_lxx_dark),
                    keyboardBackground = backgroundImage
                )
        }

        fun determineUserColor(colors: List<ColorSetting>, context: Context, colorName: String, isNight: Boolean): Int {
            val c = colors.firstOrNull { it.name == colorName }
            val color = c?.color
            val auto = c?.auto ?: true
            return if (auto || color == null)
                determineAutoColor(colors, colorName, isNight, context)
            else color
        }

        private fun determineAutoColor(colors: List<ColorSetting>, colorName: String, isNight: Boolean, context: Context): Int {
            when (colorName) {
                COLOR_ACCENT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        // try determining accent color on Android 10 & 11, accent is not available in resources
                        val wrapper: Context = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault)
                        val value = TypedValue()
                        if (wrapper.theme.resolveAttribute(android.R.attr.colorAccent, value, true)) return value.data
                    }
                    return ContextCompat.getColor(Settings.getDayNightContext(context, isNight), R.color.accent)
                }
                COLOR_GESTURE -> return determineUserColor(colors, context, COLOR_ACCENT, isNight)
                COLOR_SUGGESTION_TEXT ->
                    return determineUserColor(colors, context, COLOR_TEXT, isNight)
                COLOR_TEXT -> {
                    // base it on background color, and not key, because it's also used for suggestions
                    val background = determineUserColor(colors, context, COLOR_BACKGROUND, isNight)
                    return if (isBrightColor(background)) {
                        // but if key borders are enabled, we still want reasonable contrast
                        if (!context.prefs().getBoolean(Settings.PREF_THEME_KEY_BORDERS, Defaults.PREF_THEME_KEY_BORDERS)
                            || isGoodContrast(Color.BLACK, determineUserColor(colors, context, COLOR_KEYS, isNight))
                        ) Color.BLACK
                        else Color.GRAY
                    } else Color.WHITE
                }
                COLOR_HINT_TEXT -> {
                    return if (isBrightColor(determineUserColor(colors, context, COLOR_KEYS, isNight))) Color.DKGRAY
                    else determineUserColor(colors, context, COLOR_TEXT, isNight)
                }
                COLOR_KEYS ->
                    return brightenOrDarken(determineUserColor(colors, context, COLOR_BACKGROUND, isNight), isNight)
                COLOR_FUNCTIONAL_KEYS ->
                    return brightenOrDarken(determineUserColor(colors, context, COLOR_KEYS, isNight), true)
                COLOR_SPACEBAR -> return determineUserColor(colors, context, COLOR_KEYS, isNight)
                COLOR_SPACEBAR_TEXT -> {
                    val spacebar = determineUserColor(colors, context, COLOR_SPACEBAR, isNight)
                    val hintText = determineUserColor(colors, context, COLOR_HINT_TEXT, isNight)
                    if (isGoodContrast(hintText, spacebar)) return hintText and -0x7f000001 // add some transparency
                    val text = determineUserColor(colors, context, COLOR_TEXT, isNight)
                    if (isGoodContrast(text, spacebar)) return text and -0x7f000001
                    return if (isBrightColor(spacebar)) Color.BLACK and -0x7f000001
                    else Color.WHITE and -0x7f000001
                }
                COLOR_BACKGROUND -> return ContextCompat.getColor(
                    Settings.getDayNightContext(context, isNight),
                    R.color.keyboard_background
                )
                else -> return ContextCompat.getColor(Settings.getDayNightContext(context, isNight), R.color.keyboard_background)
            }
        }
    }
}

@Serializable
data class ColorSetting(val name: String, val auto: Boolean?, val color: Int?) {
    var displayName = name
}
