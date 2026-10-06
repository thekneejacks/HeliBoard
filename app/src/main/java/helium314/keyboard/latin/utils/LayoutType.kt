package helium314.keyboard.latin.utils

import helium314.keyboard.latin.common.Constants.Separators
import helium314.keyboard.latin.common.Constants.Subtype.ExtraValue
import java.io.File
import java.util.EnumMap

enum class LayoutType {
    MAIN, SYMBOLS, MORE_SYMBOLS, FUNCTIONAL, NUMBER, NUMBER_ROW, NUMPAD,
    NUMPAD_LANDSCAPE, PHONE, PHONE_SYMBOLS,
;
    companion object {
        fun EnumMap<LayoutType, String>.toExtraValue() = map { it.key.name + Separators.KV + it.value }.joinToString(Separators.ENTRY)

        fun getLayoutMap(string: String?): EnumMap<LayoutType, String> {
            val map = EnumMap<LayoutType, String>(LayoutType::class.java)
            string?.split(Separators.ENTRY)?.forEach {
                val s = it.split(Separators.KV)
                runCatching { map[LayoutType.valueOf(s[0])] = s[1] }
            }
            return map
        }

        val LayoutType.folder get() = "layouts${File.separator}${name.lowercase()}"

        fun getMainLayoutFromExtraValue(extraValue: String): String? {
            val value = extraValue.split(",")
                .firstOrNull { it.startsWith("${ExtraValue.KEYBOARD_LAYOUT_SET}=") }?.substringAfter("=")
            if (value == null) return null
            return getLayoutMap(value)[MAIN]
        }
    }
}
