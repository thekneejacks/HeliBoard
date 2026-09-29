// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppsManager(val context: Context) : BroadcastReceiver() {
    private var listener: AppsChangedListener? = null

    fun close() {
        context.unregisterReceiver(this)
        listener = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
            listener?.onAppsChanged()
    }

    interface AppsChangedListener {
        fun onAppsChanged()
    }

}
