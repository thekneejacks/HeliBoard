// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.utils

import android.content.Context
import helium314.keyboard.latin.common.decodeBase36
import helium314.keyboard.latin.utils.LayoutType.Companion.folder
import helium314.keyboard.latin.utils.ScriptUtils.script
import java.io.File
import java.util.EnumMap
import java.util.Locale

object LayoutUtilsCustom {

    fun getLayoutFiles(layoutType: LayoutType, context: Context, locale: Locale? = null): List<File> {
        val layouts = customLayoutMap.getOrPut(layoutType) {
            File(DeviceProtectedUtils.getFilesDir(context), layoutType.folder).listFiles()?.toList() ?: emptyList()
        }
        if (layoutType != LayoutType.MAIN || locale == null)
            return layouts
        if (locale.script() == ScriptUtils.SCRIPT_LATIN)
            return layouts.filter { it.name.startsWith(CUSTOM_LAYOUT_PREFIX + ScriptUtils.SCRIPT_LATIN + ".") }
        return layouts.filter { it.name.startsWith(CUSTOM_LAYOUT_PREFIX + locale.toLanguageTag() + ".") }
    }

    fun getDisplayName(layoutName: String) =
        try {
            if (layoutName.count { it == '.' } == 3) // main layout: "custom.<locale or script>.<name>.", other: custom.<name>.
                decodeBase36(layoutName.substringAfter(CUSTOM_LAYOUT_PREFIX).substringAfter(".").substringBeforeLast("."))
            else decodeBase36(layoutName.substringAfter(CUSTOM_LAYOUT_PREFIX).substringBeforeLast("."))
        } catch (_: NumberFormatException) {
            layoutName
        }

    fun isCustomLayout(layoutName: String) = layoutName.startsWith(CUSTOM_LAYOUT_PREFIX)

    // remove layouts without a layout file from custom subtypes and settings
    // should not be necessary, but better fall back to default instead of crashing when encountering a bug
    /*fun removeMissingLayouts(context: Context) {
        val prefs = context.prefs()
        fun remove(type: LayoutType, name: String) {
            //Log.w(TAG, "removing custom layout ${getDisplayName(name)} / $name without file")
            SubtypeSettings.onRenameLayout(type, name, null, context)
        }
        LayoutType.entries.forEach { type ->
            val name = Settings.readDefaultLayoutName(type, prefs)
            if (!isCustomLayout(name) || getLayoutFiles(type, context).any { it.name.startsWith(name) })
                return@forEach
            remove(type, name)
        }
        prefs.getString(Settings.PREF_ADDITIONAL_SUBTYPES, Defaults.PREF_ADDITIONAL_SUBTYPES)!!
            .split(Separators.SETS).forEach outer@{
                val subtype = it.toSettingsSubtype()
                LayoutType.getLayoutMap(subtype.getExtraValueOf(KEYBOARD_LAYOUT_SET) ?: "").forEach { (type, name) ->
                    if (!isCustomLayout(name) || getLayoutFiles(type, context).any { it.name.startsWith(name) })
                        return@forEach
                    remove(type, name)
                    // recursive call: additional subtypes must have changed, so we repeat until nothing needs to be deleted
                    removeMissingLayouts(context)
                    return
                }
            }
    }*/

    // this goes into prefs and file names, so do not change!
    const val CUSTOM_LAYOUT_PREFIX = "custom."
    private const val TAG = "LayoutUtilsCustom"
    private val customLayoutMap = EnumMap<LayoutType, List<File>>(LayoutType::class.java)
}
