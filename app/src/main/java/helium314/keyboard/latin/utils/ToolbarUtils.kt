// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import helium314.keyboard.keyboard.internal.KeyboardIconsSet
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.R
import helium314.keyboard.latin.common.Constants.Separators
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ToolbarKey.CLEAR_CLIPBOARD
import helium314.keyboard.latin.utils.ToolbarKey.COPY
import helium314.keyboard.latin.utils.ToolbarKey.CUT
import helium314.keyboard.latin.utils.ToolbarKey.DOWN
import helium314.keyboard.latin.utils.ToolbarKey.LEFT
import helium314.keyboard.latin.utils.ToolbarKey.NUMPAD
import helium314.keyboard.latin.utils.ToolbarKey.PAGE_DOWN
import helium314.keyboard.latin.utils.ToolbarKey.PAGE_END
import helium314.keyboard.latin.utils.ToolbarKey.PAGE_START
import helium314.keyboard.latin.utils.ToolbarKey.PAGE_UP
import helium314.keyboard.latin.utils.ToolbarKey.PASTE
import helium314.keyboard.latin.utils.ToolbarKey.REDO
import helium314.keyboard.latin.utils.ToolbarKey.RIGHT
import helium314.keyboard.latin.utils.ToolbarKey.SELECT_ALL
import helium314.keyboard.latin.utils.ToolbarKey.SELECT_WORD
import helium314.keyboard.latin.utils.ToolbarKey.UNDO
import helium314.keyboard.latin.utils.ToolbarKey.UP
import helium314.keyboard.latin.utils.ToolbarKey.WORD_LEFT
import helium314.keyboard.latin.utils.ToolbarKey.WORD_RIGHT
import helium314.keyboard.latin.utils.ToolbarKey.entries
import java.util.EnumMap
import java.util.Locale

fun createToolbarKey(context: Context, key: ToolbarKey): ImageButton {
    val button = ImageButton(context, null, R.attr.suggestionWordStyle)
    button.scaleType = ImageView.ScaleType.CENTER
    button.tag = key
    button.contentDescription = key.name.lowercase().getStringResourceOrName("", context)
    button.isActivated = true
    button.setImageDrawable(KeyboardIconsSet.instance.getNewDrawable(key.name, context))
    return button
}

/*fun setToolbarButtonsActivatedStateOnPrefChange(buttonsGroup: ViewGroup, key: String?) {
    // settings need to be updated when buttons change
    if (key?.startsWith(Settings.PREF_ONE_HANDED_MODE_PREFIX) == false)
        return

    Handler(Looper.getMainLooper()).postDelayed({
        buttonsGroup.forEach { if (it is ImageButton) setToolbarButtonActivatedState(it) }
    }, 10)
}*/

fun getCodeForToolbarKey(key: ToolbarKey) = Settings.getInstance().getCustomToolbarKeyCode(key) ?: when (key) {
    NUMPAD -> KeyCode.NUMPAD
    UNDO -> KeyCode.UNDO
    REDO -> KeyCode.REDO
    SELECT_ALL -> KeyCode.CLIPBOARD_SELECT_ALL
    SELECT_WORD -> KeyCode.CLIPBOARD_SELECT_WORD
    COPY -> KeyCode.CLIPBOARD_COPY
    CUT -> KeyCode.CLIPBOARD_CUT
    PASTE -> KeyCode.CLIPBOARD_PASTE
    CLEAR_CLIPBOARD -> KeyCode.CLIPBOARD_CLEAR_HISTORY
    LEFT -> KeyCode.ARROW_LEFT
    RIGHT -> KeyCode.ARROW_RIGHT
    UP -> KeyCode.ARROW_UP
    DOWN -> KeyCode.ARROW_DOWN
    WORD_LEFT -> KeyCode.WORD_LEFT
    WORD_RIGHT -> KeyCode.WORD_RIGHT
    PAGE_UP -> KeyCode.PAGE_UP
    PAGE_DOWN -> KeyCode.PAGE_DOWN
    PAGE_START -> KeyCode.MOVE_START_OF_PAGE
    PAGE_END -> KeyCode.MOVE_END_OF_PAGE
}

fun getCodeForToolbarKeyLongClick(key: ToolbarKey) = Settings.getInstance().getCustomToolbarLongpressCode(key) ?: when (key) {
    UNDO -> KeyCode.REDO
    REDO -> KeyCode.UNDO
    SELECT_ALL -> KeyCode.CLIPBOARD_SELECT_WORD
    SELECT_WORD -> KeyCode.CLIPBOARD_SELECT_ALL
    COPY -> KeyCode.CLIPBOARD_CUT
    PASTE -> KeyCode.CLIPBOARD
    LEFT -> KeyCode.KEY_REPEAT
    RIGHT -> KeyCode.KEY_REPEAT
    UP -> KeyCode.KEY_REPEAT
    DOWN -> KeyCode.KEY_REPEAT
    WORD_LEFT -> KeyCode.KEY_REPEAT
    WORD_RIGHT -> KeyCode.KEY_REPEAT
    PAGE_UP -> KeyCode.MOVE_START_OF_PAGE
    PAGE_DOWN -> KeyCode.MOVE_END_OF_PAGE
    //BACKGROUND_GATHERING -> KeyCode.BACKGROUND_GATHERING_TEMP_OFF
    else -> KeyCode.UNSPECIFIED
}

// names need to be aligned with resources strings (using lowercase of key.name)
enum class ToolbarKey {
    NUMPAD, UNDO, REDO, SELECT_ALL, SELECT_WORD, COPY, CUT, PASTE,
    CLEAR_CLIPBOARD,LEFT, RIGHT, UP, DOWN, WORD_LEFT, WORD_RIGHT,
    PAGE_UP, PAGE_DOWN, PAGE_START, PAGE_END
}

enum class ToolbarMode {
    EXPANDABLE, TOOLBAR_KEYS, HIDDEN,
}

val toolbarKeyStrings = entries.associateWithTo(EnumMap(ToolbarKey::class.java)) { it.toString().lowercase(Locale.US) }

val defaultToolbarPref by lazy {
    val default = listOf(UNDO, REDO, SELECT_WORD, COPY, PASTE, LEFT, RIGHT)
    val others = entries.filterNot { it in default }
    default.joinToString(Separators.ENTRY) { it.name + Separators.KV + true } + Separators.ENTRY +
            others.joinToString(Separators.ENTRY) { it.name + Separators.KV + false }
}

fun getEnabledToolbarKeys(prefs: SharedPreferences) = getEnabledToolbarKeys(prefs, Settings.PREF_TOOLBAR_KEYS, defaultToolbarPref)

private fun getEnabledToolbarKeys(prefs: SharedPreferences, pref: String, default: String): List<ToolbarKey> {
    val string = prefs.getString(pref, default)!!
    return string.split(Separators.ENTRY).mapNotNull {
        val split = it.split(Separators.KV)
        if (split.last() == "true") {
            try {
                ToolbarKey.valueOf(split.first())
            } catch (_: IllegalArgumentException) {
                null
            }
        } else null
    }
}

fun readCustomKeyCodes(prefs: SharedPreferences): EnumMap<ToolbarKey, Pair<Int?, Int?>> {
    val map = EnumMap<ToolbarKey, Pair<Int?, Int?>>(ToolbarKey::class.java)
    prefs.getString(Settings.PREF_TOOLBAR_CUSTOM_KEY_CODES, Defaults.PREF_TOOLBAR_CUSTOM_KEY_CODES)!!
        .split(";").forEach {
            runCatching {
                val s = it.split(",")
                map[ToolbarKey.valueOf(s[0])] = s[1].toIntOrNull() to s[2].toIntOrNull()
            }
        }
    return map
}

fun getCustomKeyCode(key: ToolbarKey, prefs: SharedPreferences): Int? {
    if (customToolbarKeyCodes == null)
        customToolbarKeyCodes = readCustomKeyCodes(prefs)
    return customToolbarKeyCodes!![key]?.first
}

fun getCustomLongpressKeyCode(key: ToolbarKey, prefs: SharedPreferences): Int? {
    if (customToolbarKeyCodes == null)
        customToolbarKeyCodes = readCustomKeyCodes(prefs)
    return customToolbarKeyCodes!![key]?.second
}

fun clearCustomToolbarKeyCodes() {
    customToolbarKeyCodes = null
}

fun onClickToolbarKey(view: View, onCodeInput: (Int) -> Unit) {
    //AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(KeyCode.NOT_SPECIFIED, view, HapticEvent.KEY_PRESS)
    val code = getCodeForToolbarKey(view.tag as ToolbarKey)
    if (code != KeyCode.UNSPECIFIED) {
        onCodeInput(code)
    }
}

fun onLongClickToolbarKey(view: View, onCodeInput: (Int, Boolean) -> Unit) {
    //AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(KeyCode.NOT_SPECIFIED, view, HapticEvent.KEY_LONG_PRESS)
    val longClickCode = getCodeForToolbarKeyLongClick(view.tag as ToolbarKey)
    if (longClickCode == KeyCode.KEY_REPEAT) {
        onClickToolbarKey(view) { onCodeInput(it, false) }
        repeatToolbarKey(view) { onClickToolbarKey(view) { onCodeInput(it, true) } }
    } else if (longClickCode != KeyCode.UNSPECIFIED) {
        onCodeInput(longClickCode, false)
    }
}

private fun repeatToolbarKey(view: View, onClick: (view: View) -> Unit) {
    view.handler.postDelayed({
        if (view.isPressed) {
            onClick(view)
            repeatToolbarKey(view, onClick)
        }
    }, view.resources.getInteger(R.integer.config_key_repeat_interval).toLong())
}

private var customToolbarKeyCodes: EnumMap<ToolbarKey, Pair<Int?, Int?>>? = null
