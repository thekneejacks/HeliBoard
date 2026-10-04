// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package helium314.keyboard.latin

import android.app.Application
import helium314.keyboard.latin.define.DebugFlags
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.FoldableUtils
import helium314.keyboard.latin.utils.LayoutUtilsCustom
import helium314.keyboard.latin.utils.SubtypeSettings
import helium314.keyboard.latin.utils.prefs
import helium314.keyboard.latin.utils.upgradeToolbarPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugFlags.init(this)
        FoldableUtils.init(this)
        Settings.init(this)
        SubtypeSettings.init(this)

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch { // do some uncritical work in background for faster startup
            LayoutUtilsCustom.removeMissingLayouts(this@App)
        }

        RichInputMethodManager.init(this)
        app = this
        Defaults.initDynamicDefaults(this)
    }

    companion object {
        // used so JniUtils can access application once
        private var app: App? = null
        fun getApp(): App? {
            val application = app
            app = null
            return application
        }
    }
}
