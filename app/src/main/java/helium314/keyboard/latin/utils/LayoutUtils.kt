package helium314.keyboard.latin.utils

import android.content.Context
import helium314.keyboard.latin.settings.Defaults.default
import helium314.keyboard.latin.utils.LayoutType.Companion.folder
import java.io.File

// for layouts provided by the app
object LayoutUtils {

    /** gets content for built-in (non-custom) layout [layoutName], with fallback to qwerty */
    fun getContent(layoutType: LayoutType, layoutName: String, context: Context): String {
        val layouts = context.assets.list(layoutType.folder)!!
        layouts.firstOrNull { it.startsWith("$layoutName.") }
            ?.let { return context.assets.open(layoutType.folder + File.separator + it).reader().readText() }
        val fallback = layouts.first { it.startsWith(layoutType.default) } // must exist!
        return context.assets.open(layoutType.folder + File.separator + fallback).reader().readText()
    }

    fun getSimpleRowStrings(layoutContent: String): List<String> =
        layoutContent.replace("\r\n", "\n").split("\\n\\s*\\n".toRegex()).filter { it.isNotBlank() }
}
