// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package helium314.keyboard.latin

import android.app.Application
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.SubtypeSettings


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Settings.init(this)
        SubtypeSettings.init(this)

        RichInputMethodManager.init(this)
        Defaults.initDynamicDefaults(this)

        this.deleteDatabase("heliboard.db")
    }
}
