// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.inputmethod.InputMethodSubtype
import androidx.core.content.edit
import helium314.keyboard.latin.RichInputMethodManager
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.settings.SettingsSubtype
import helium314.keyboard.latin.settings.SettingsSubtype.Companion.toSettingsSubtype
import java.util.Locale

object SubtypeSettings {
    /** @return enabled subtypes. If no subtypes are enabled, but a contextForFallback is provided,
     *  subtypes for system locales will be returned, or en-US if none found. */
    fun getEnabledSubtypes(): List<InputMethodSubtype> {
        val enabled = getDefaultEnabledSubtypes()
        return if (Settings.getValues()?.mIsLocked != true) enabled // on app start SettingsValues are null
        else enabled + SettingsSubtype.fallbackSubtype.toAdditionalSubtype()
    }

    fun isEnabled(subtype: InputMethodSubtype?): Boolean = subtype in getDefaultEnabledSubtypes()

    fun getSelectedSubtype(prefs: SharedPreferences): InputMethodSubtype {
        val selectedSubtype = prefs.getString(Settings.PREF_SELECTED_SUBTYPE, Defaults.PREF_SELECTED_SUBTYPE)!!.toSettingsSubtype()

        // no additional subtype, must be a resource subtype
        val defaultSubtypes = getDefaultEnabledSubtypes()
        return defaultSubtypes.firstOrNull { it.locale() == selectedSubtype.locale && it.mainLayoutName() == it.mainLayoutName() }
            ?: defaultSubtypes.firstOrNull { it.locale().language == selectedSubtype.locale.language }
            ?: defaultSubtypes.first()
    }

    fun setSelectedSubtype(prefs: SharedPreferences, subtype: InputMethodSubtype) {
        val settingsSubtype = subtype.toSettingsSubtype()
        if (settingsSubtype.locale.toLanguageTag().isEmpty()) {
            //Log.w(TAG, "tried to set subtype with empty locale: $settingsSubtype")
            return
        }
        prefs.edit { putString(Settings.PREF_SELECTED_SUBTYPE, settingsSubtype.toPref()) }
    }

    fun getResourceSubtypesForLocale(locale: Locale): List<InputMethodSubtype> = resourceSubtypesByLocale[locale].orEmpty()

    fun reloadEnabledSubtypes() {
        if (RichInputMethodManager.isInitialized())
            RichInputMethodManager.getInstance().refreshSubtypeCaches()
    }

    fun init(context: Context) {
        SubtypeLocaleUtils.init(context) // necessary to get the correct getKeyboardLayoutSetName
        loadResourceSubtypes(context.resources)
    }

    @Suppress("SameReturnValue")
    private fun getDefaultEnabledSubtypes(): List<InputMethodSubtype> {
        if (systemSubtypes.isNotEmpty()) return systemSubtypes
        systemSubtypes.add(resourceSubtypesByLocale[Locale.US]!!.first())
        systemSubtypes.add(resourceSubtypesByLocale[Locale.KOREAN]!!.first())
        return systemSubtypes
    }

    private fun loadResourceSubtypes(resources: Resources) {
        getResourceSubtypes(resources).forEach {
            resourceSubtypesByLocale.getOrPut(it.locale()) { ArrayList(2) }.add(it)
        }
    }

    private val resourceSubtypesByLocale = LinkedHashMap<Locale, MutableList<InputMethodSubtype>>(100)
    private val systemSubtypes = mutableListOf<InputMethodSubtype>()
    private val TAG = SubtypeSettings::class.simpleName
}
