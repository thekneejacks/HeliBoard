// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.latin.settings

/** Derive an index from a number of boolean [settingValues], used to access the matching default value in a defaults arraY */
fun findIndexOfDefaultSetting(vararg settingValues: Boolean): Int {
    var i = -1
    return settingValues.sumOf { i++; if (it) 1.shl(i) else 0 }
}

/** Create pref key that is derived from a [number] of boolean conditions. The [index] is as created by [findIndexOfDefaultSetting]. */
fun createPrefKeyForBooleanSettings(prefix: String, index: Int, number: Int): String =
    "${prefix}_${Array(number) { index.shr(it) % 2 == 1 }.joinToString("_")}"

