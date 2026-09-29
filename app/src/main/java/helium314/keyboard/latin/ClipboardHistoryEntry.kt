// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin

import android.content.Context
import androidx.core.content.FileProvider
import helium314.keyboard.latin.database.ClipboardDao
import helium314.keyboard.latin.settings.Settings
import java.io.File

class ClipboardHistoryEntry(
    val id: Long,
    var timeStamp: Long,
    var isPinned: Boolean,
    val text: String?,
    val filename: String?,
    val mimeTypes: List<String>?
) : Comparable<ClipboardHistoryEntry> {
    // for display order
    override fun compareTo(other: ClipboardHistoryEntry): Int {
        val result = other.isPinned.compareTo(isPinned)
        if (result == 0) return other.timeStamp.compareTo(timeStamp)
        if (Settings.getValues()?.mClipboardHistoryPinnedFirst == false) return -result
        return result
    }

    fun getContentUri(context: Context) = filename?.let { FileProvider.getUriForFile(
        context,
        context.getString(R.string.clipboard_provider_authority),
        File(ClipboardDao.clipFilesDir, it)
    ) }

}
