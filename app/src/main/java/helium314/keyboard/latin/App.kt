// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package helium314.keyboard.latin

import android.app.Application
import android.util.Log
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.prefs


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Settings.init(this)
        SubtypeSettings.init(this)

        RichInputMethodManager.init(this)
        Defaults.initDynamicDefaults(this)

        Log.d("TAG", "onCreateHeli: " + prefs().getString(Settings.PREF_ENABLED_SUBTYPES, Defaults.PREF_ENABLED_SUBTYPES))

        this.deleteDatabase("heliboard.db")
    }
}
