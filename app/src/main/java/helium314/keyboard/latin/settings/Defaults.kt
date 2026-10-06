package helium314.keyboard.latin.settings

import android.content.Context
import android.os.Build
import android.util.TypedValue
import helium314.keyboard.keyboard.KeyboardActionListener
import helium314.keyboard.latin.common.Constants.Separators
import helium314.keyboard.latin.common.Constants.Subtype.ExtraValue
import helium314.keyboard.latin.utils.LayoutType
import helium314.keyboard.latin.utils.POPUP_KEYS_LABEL_DEFAULT
import helium314.keyboard.latin.utils.POPUP_KEYS_ORDER_DEFAULT

object Defaults {
    fun initDynamicDefaults(context: Context) {
        val dm = context.resources.displayMetrics
        val px600 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600f, dm)
        PREF_POPUP_ON = dm.widthPixels >= px600 || dm.heightPixels >= px600
    }

    // must correspond to a file name
    val LayoutType.default get() = when (this) {
        LayoutType.MAIN -> "qwerty"
        LayoutType.SYMBOLS -> "symbols"
        LayoutType.MORE_SYMBOLS -> "symbols_shifted"
        LayoutType.FUNCTIONAL -> if (Settings.getInstance().isTablet) "functional_keys_tablet" else "functional_keys"
        LayoutType.NUMBER -> "number"
        LayoutType.NUMBER_ROW -> "number_row"
        LayoutType.NUMPAD -> "numpad"
        LayoutType.NUMPAD_LANDSCAPE -> "numpad_landscape"
        LayoutType.PHONE -> "phone"
        LayoutType.PHONE_SYMBOLS -> "phone_symbols"
        LayoutType.EMOJI_BOTTOM -> "emoji_bottom_row"
        LayoutType.CLIPBOARD_BOTTOM -> "clip_bottom_row"
    }

    private const val DEFAULT_SIZE_SCALE = 1.0f // 100%
    const val PREF_THEME_KEY_BORDERS = false
    const val PREF_TOOLBAR_CUSTOM_KEY_CODES = ""
    const val PREF_AUTO_CAP = true
    @JvmField
    var PREF_POPUP_ON = true
    const val PREF_KEY_USE_DOUBLE_SPACE_PERIOD = true
    const val PREF_LANGUAGE_SWITCH_KEY = "internal"
    const val PREF_VARIABLE_TOOLBAR_DIRECTION = true
    const val PREF_ADDITIONAL_SUBTYPES = "de${Separators.SET}${ExtraValue.KEYBOARD_LAYOUT_SET}=MAIN:qwerty${Separators.SETS}" +
            "fr${Separators.SET}${ExtraValue.KEYBOARD_LAYOUT_SET}=MAIN:qwertz${Separators.SETS}" +
            "hu${Separators.SET}${ExtraValue.KEYBOARD_LAYOUT_SET}=MAIN:qwerty"

    @JvmField
    val PREF_KEYBOARD_HEIGHT_SCALE = Array(4) { DEFAULT_SIZE_SCALE }
    @JvmField
    val PREF_BOTTOM_ROW_SCALE = Array(4) { DEFAULT_SIZE_SCALE }
    @JvmField
    // DEFAULT_SIZE_SCALE for portrait, 0 for landscape (normal and folded)
    val PREF_BOTTOM_PADDING_SCALE = arrayOf(DEFAULT_SIZE_SCALE, 0f, DEFAULT_SIZE_SCALE, 0f)
    @JvmField
    val PREF_SIDE_PADDING_SCALE = Array(8) { 0f }
    @JvmField
    val PREF_KEY_GAP_SCALE = Array(4) { DEFAULT_SIZE_SCALE }
    const val PREF_FONT_SCALE = DEFAULT_SIZE_SCALE
    const val PREF_HINT_FONT_SCALE = DEFAULT_SIZE_SCALE
    const val PREF_EMOJI_FONT_SCALE = DEFAULT_SIZE_SCALE

    @JvmField
    val PREF_SPACE_HORIZONTAL_SWIPE = KeyboardActionListener.SwipeAction.MOVE_CURSOR.name
    @JvmField
    val PREF_SPACE_VERTICAL_SWIPE = KeyboardActionListener.SwipeAction.NONE.name
    const val PREF_DELETE_SWIPE = true
    const val PREF_AUTOSPACE_AFTER_PUNCTUATION = false
    const val PREF_AUTOSPACE_BEFORE_GESTURE_TYPING = true
    const val PREF_SHIFT_REMOVES_AUTOSPACE = false
    const val PREF_KEY_LONGPRESS_TIMEOUT = 300

    const val PREF_GESTURE_FAST_TYPING_COOLDOWN = 500
    const val PREF_GESTURE_TRAIL_FADEOUT_DURATION = 800
    const val PREFS_LONG_PRESS_SYMBOLS_FOR_NUMPAD = false

    const val PREF_SHOW_NUMBER_ROW = false
    const val PREF_SHOW_NUMBER_ROW_IN_SYMBOLS = true
    const val PREF_LOCALIZED_NUMBER_ROW = true
    const val PREF_SHOW_NUMBER_ROW_HINTS = false
    const val PREF_CUSTOM_CURRENCY_KEY = ""
    const val PREF_SHOW_HINTS = true
    const val PREF_POPUP_KEYS_ORDER = POPUP_KEYS_ORDER_DEFAULT
    const val PREF_POPUP_KEYS_HINT_ORDER = POPUP_KEYS_LABEL_DEFAULT
    const val PREF_SHOW_POPUP_HINTS = false
    const val PREF_SHOW_TLD_POPUP_KEYS = true
    const val PREF_MORE_POPUP_KEYS = "main"
    const val PREF_SPACE_TO_CHANGE_LANG = true
    const val PREF_LANGUAGE_SWIPE_DISTANCE = 5
    const val PREF_TOUCHPAD_SENSITIVITY = 50
    const val PREF_TOUCHPAD_EDGE_SCROLL = true

    @JvmField
    val PREF_NAVBAR_COLOR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    const val PREF_ENABLED_SUBTYPES = ""
    const val PREF_SELECTED_SUBTYPE = ""
    const val PREF_TOOLBAR_MODE = "EXPANDABLE"
    const val PREF_TOOLBAR_HIDING_GLOBAL = true
    const val PREF_QUICK_PIN_TOOLBAR_KEYS = false
    const val PREF_AUTO_SHOW_TOOLBAR = false
    const val PREF_ABC_AFTER_SYMBOL_SPACE = true
    const val PREF_ABC_AFTER_NUMPAD_SPACE = false
    const val PREF_REMOVE_REDUNDANT_POPUPS = false
    const val PREF_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val PREF_SHOW_SUGGESTION_INFOS = false
    const val PREF_FORCE_NON_DISTINCT_MULTITOUCH = false
    const val PREF_SAVE_SUBTYPE_PER_APP = false
    const val PREF_SHOW_ONLY_TOOLBAR_WITH_HARDWARE_KEYBOARD = false
}
