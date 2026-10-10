package helium314.keyboard.keyboard.internal.keyboard_parser.floris

import helium314.keyboard.keyboard.KeyboardElement
import helium314.keyboard.keyboard.internal.KeyboardCodesSet
import helium314.keyboard.keyboard.internal.KeyboardParams
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.ToolbarKey
import helium314.keyboard.latin.utils.getCodeForToolbarKey
import helium314.keyboard.latin.utils.toolbarKeyStrings
import java.util.Locale

/** labels for functional / special keys */
object KeyLabel {
    const val COM = "com"
    const val LANGUAGE_SWITCH = "language_switch"
    const val ACTION = "action"
    const val DELETE = "delete"
    const val SHIFT = "shift"
    const val NUMPAD = "numpad"
    const val SYMBOL = "symbol"
    const val ALPHA = "alpha"
    const val SYMBOL_ALPHA = "symbol_alpha"
    const val PERIOD = "period"
    const val COMMA = "comma"
    const val SPACE = "space"
    const val ZWNJ = "zwnj"
    const val CURRENCY = "$$$"
    const val CURRENCY1 = "$$$1"
    const val CURRENCY2 = "$$$2"
    const val CURRENCY3 = "$$$3"
    const val CURRENCY4 = "$$$4"
    const val CURRENCY5 = "$$$5"
    const val CTRL = "ctrl"
    const val ALT = "alt"
    const val FN = "fn"
    const val META = "meta"
    const val TAB = "tab"
    const val ESCAPE = "esc"

    /** to make sure a FlorisBoard label works when reading a JSON layout */
    // resulting special labels should be names of FunctionalKey enum, case insensitive
    fun String.convertFlorisLabel(): String = when (this) {
        "view_characters" -> ALPHA
        "view_symbols" -> SYMBOL
        "view_numeric_advanced" -> NUMPAD
        "view_phone" -> ALPHA // phone keyboard is treated like alphabet, just with different layout
        "view_phone2" -> SYMBOL // phone symbols
        "ime_ui_mode_text" -> ALPHA
        "currency_slot_1" -> CURRENCY
        "currency_slot_2" -> CURRENCY1
        "currency_slot_3" -> CURRENCY2
        "currency_slot_4" -> CURRENCY3
        "currency_slot_5" -> CURRENCY4
        "currency_slot_6" -> CURRENCY5
        "enter" -> ACTION
        else -> this
    }

    fun String.rtlLabel(params: KeyboardParams): String {
        if (!params.mId.subtype.isRtlSubtype || params.mId.element.isNumberLayout) return this
        return when (this) {
            "{" -> "{|}"
            "}" -> "}|{"
            "(" -> "(|)"
            ")" -> ")|("
            "[" -> "[|]"
            "]" -> "]|["
            "<" -> "<|>"
            ">" -> ">|<"
            "≤" -> "≤|≥"
            "≥" -> "≥|≤"
            "«" -> "«|»"
            "»" -> "»|«"
            "‹" -> "‹|›"
            "›" -> "›|‹"
            "﴾" -> "﴾|﴿"
            "﴿" -> "﴿|﴾"
            "⁽" -> "⁽|⁾"
            "⁾" -> "⁾|⁽"
            else -> this
        }
    }

    fun keyLabelToActualLabel(label: String, params: KeyboardParams): String {
        val newLabel = when (label) {
            SYMBOL_ALPHA -> if (params.mId.element.isAlphabet) {
                params.mLocaleKeyboardInfos.labelSymbol
            } else {
                params.mLocaleKeyboardInfos.labelAlphabet
            }
            SYMBOL -> params.mLocaleKeyboardInfos.labelSymbolInNumpad
            ALPHA -> params.mLocaleKeyboardInfos.labelAlphabet
            COMMA -> params.mLocaleKeyboardInfos.labelComma
            PERIOD -> getPeriodLabel(params)
            SPACE -> "${getSpaceLabel}|!code/key_space"
            ACTION -> "${getActionKeyLabel}|${getActionKeyCode(params)}"
            DELETE -> "${getDeleteLabel}|!code/key_delete"
            SHIFT -> "${getShiftLabel(params)}|!code/key_shift"
            COM -> params.mLocaleKeyboardInfos.tlds.first()
            CURRENCY -> params.mLocaleKeyboardInfos.currencyKey.first
            CURRENCY1 -> params.mLocaleKeyboardInfos.currencyKey.second[0]
            CURRENCY2 -> params.mLocaleKeyboardInfos.currencyKey.second[1]
            CURRENCY3 -> params.mLocaleKeyboardInfos.currencyKey.second[2]
            CURRENCY4 -> params.mLocaleKeyboardInfos.currencyKey.second[3]
            CURRENCY5 -> params.mLocaleKeyboardInfos.currencyKey.second[4]
            LANGUAGE_SWITCH -> "${"⇄"}|!code/key_language_switch"
            CTRL, ALT, FN, META, ESCAPE -> label.uppercase(Locale.US)
            TAB -> "!icon/tab_key|!code/${KeyCode.TAB}"
            else -> if (label in toolbarKeyStrings.values)
                "!icon/$label|!code/${getCodeForToolbarKey(ToolbarKey.valueOf(label.uppercase(Locale.US)))}"
            else label
        }
        val code = when (label) { // maybe a bit lazy to not assemble the entire string above
            SYMBOL_ALPHA -> KeyCode.SYMBOL_ALPHA
            SYMBOL       -> KeyCode.SYMBOL
            ALPHA        -> KeyCode.ALPHA
            CTRL         -> KeyCode.CTRL
            ALT          -> KeyCode.ALT
            FN           -> KeyCode.FN
            META         -> KeyCode.META
            ESCAPE       -> KeyCode.ESCAPE
            else         -> null
        }
        return if (code == null) newLabel
        else "$newLabel|!code/$code"
    }

    private fun getShiftLabel(params: KeyboardParams) = when (params.mId.element) {
        KeyboardElement.SYMBOLS_SHIFTED -> params.mLocaleKeyboardInfos.labelSymbol
        KeyboardElement.SYMBOLS -> params.mLocaleKeyboardInfos.getShiftSymbolLabel(
            Settings.getInstance().isTablet)
        //KeyboardElement.ALPHABET_MANUAL_SHIFTED, KeyboardElement.ALPHABET_AUTOMATIC_SHIFTED -> "!icon/${KeyboardIconsSet.NAME_SHIFT_KEY_SHIFTED}"
        KeyboardElement.ALPHABET_MANUAL_SHIFTED, KeyboardElement.ALPHABET_AUTOMATIC_SHIFTED -> "⇪"
        KeyboardElement.ALPHABET_SHIFT_LOCKED -> "⇪"

        else -> "⇧"
    }

    private val getDeleteLabel : String = "⌫"

    // todo (later): try avoiding this weirdness
    //  maybe just remove it and if users want it they can use custom functional layouts?
    //  but it has been like this "forever" and actually seems to make sense
    private fun getPeriodLabel(params: KeyboardParams): String {
        if (params.mId.element.isNumberLayout) return "."
        if (params.mId.element.isAlphabet || params.mId.locale.language == "ar" || params.mId.locale.language == "fa")
            return params.mLocaleKeyboardInfos.labelPeriod
        return "."
    }

    // todo (later): should this be handled with metaState? but metaState shift would require LOTS of changes...
    private fun getActionKeyCode(params: KeyboardParams): String {
        params.mId.internalAction?.let { return "${KeyboardCodesSet.PREFIX_CODE}${it.code}" }
        return if (params.mId.isMultiLine && params.mId.element == KeyboardElement.ALPHABET_MANUAL_SHIFTED)
            "!code/key_shift_enter"
        else "!code/key_enter"
    }

    private val getActionKeyLabel : String = "GO"

    private val getSpaceLabel : String = " "
}
