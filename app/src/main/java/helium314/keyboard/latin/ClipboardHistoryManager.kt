// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.text.TextUtils
import android.view.View
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.core.view.isGone
import helium314.keyboard.compat.ClipboardManagerCompat
import helium314.keyboard.event.Event
import helium314.keyboard.keyboard.internal.keyboard_parser.floris.KeyCode
import helium314.keyboard.latin.common.Constants
import helium314.keyboard.latin.database.ClipboardDao
import helium314.keyboard.latin.settings.Defaults
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.InputTypeUtils
import helium314.keyboard.latin.utils.Log
import helium314.keyboard.latin.utils.prefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClipboardHistoryManager(
        private val latinIME: LatinIME
) : ClipboardManager.OnPrimaryClipChangedListener {

    private lateinit var clipboardManager: ClipboardManager
    private var clipboardSuggestionView: View? = null
    private var clipboardDao: ClipboardDao? = null
    private var tempPrimaryClip = false

    fun onCreate() {
        clipboardManager = latinIME.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(this)
        clipboardDao = ClipboardDao.getInstance(latinIME)
        if (latinIME.mSettings.current.mClipboardHistoryEnabled)
            fetchPrimaryClip()
    }

    fun onDestroy() {
        clipboardManager.removePrimaryClipChangedListener(this)
    }

    override fun onPrimaryClipChanged() {
        // Make sure we read clipboard content only if history settings is set
        if (latinIME.mSettings.current.mClipboardHistoryEnabled) {
            fetchPrimaryClip()
            dontShowCurrentSuggestion = false
        }
    }

    // todo for later
    //  setting whether to store sensitive clip data?
    //  care about other clip items than first?
    private fun fetchPrimaryClip() {
        if (tempPrimaryClip) return // avoid updating history
        val clipData = clipboardManager.primaryClip ?: return
        if (clipData.itemCount == 0) return
        val clipItem = clipData.getItemAt(0) ?: return
        val description = clipData.description ?: return
        val timeStamp = ClipboardManagerCompat.getClipTimestamp(clipData)

        if (description.hasMimeType("text/*")) {
            val content = clipItem.coerceToText(latinIME)
            if (TextUtils.isEmpty(content)) return
            clipboardDao?.addClip(timeStamp, false, content.toString())
        } else if (maySaveFromUri(clipItem.uri, latinIME)) {
            clipboardDao?.addClipUri(timeStamp, false, clipItem.uri, description, latinIME)
        }
    }

    fun getPrimaryClipIfText(): String? {
        if (tempPrimaryClip) return null // avoid updating history
        val clipData = clipboardManager.primaryClip ?: return null
        if (clipData.itemCount == 0) return null
        val clipItem = clipData.getItemAt(0) ?: return null
        return if (clipData.description?.hasMimeType("text/*") == true)
            clipItem.coerceToText(latinIME).toString().takeIf { it.isNotEmpty() }
        else null
    }

    // fallback method because in some apps there is no supported mime type and commitContend does nothing,
    // but KeyEvent.KEYCODE_PASTE for pasting from primary clip works fine
    // (actually we do change the primary clip, but (try to) revert immediately)
    fun pasteWithoutChangingClips(content: InputContentInfoCompat) {
        Log.d(TAG, "trying fallback pasting with system clipboard")
        val primaryClip = clipboardManager.primaryClip
        val tempClip = ClipData(content.description, ClipData.Item(content.contentUri))
        tempPrimaryClip = true
        clipboardManager.setPrimaryClip(tempClip)
        latinIME.onEvent(Event.createSoftwareKeypressEvent(KeyCode.CLIPBOARD_PASTE, 0,
            Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE, false))
        tempPrimaryClip = false
        if (primaryClip == null)
            return
        // we need to wait a little before switching back to the original primary clip
        // a. it can happen that we switch back before the pasting has started, in that case we only past the primary clip
        // b. if we switch while the clip is pasted, it might crash the app (tested with joplin and logseq)
        // todo: replacing the current primary clip is far from ideal, try finding a different way
        GlobalScope.launch {
            delay(500)
            try {
                clipboardManager.setPrimaryClip(primaryClip)
            } catch (e: Exception) {
                Log.i(TAG, "could not go back to old primary clip", e)
                // happens wen the clip was a file
                // try to find it in out clipboard entries
                val clip = clipboardDao?.getAll()?.firstOrNull { it.timeStamp == ClipboardManagerCompat.getClipTimestamp(primaryClip) }
                if (clip?.filename != null)
                    clipboardManager.setPrimaryClip(ClipData(
                        ClipDescription(clip.text, clip.mimeTypes?.toTypedArray()),
                        ClipData.Item(clip.getContentUri(latinIME))
                    ))
                else if (clip != null)
                    clipboardManager.setPrimaryClip(ClipData(
                        ClipDescription("", arrayOf("text/*")),
                        ClipData.Item(clip.text)
                    ))
            }
        }
    }

    private fun removeClipboardSuggestion() {
        dontShowCurrentSuggestion = true
        val csv = clipboardSuggestionView ?: return
        if (csv.parent != null && !csv.isGone) {
            // clipboard view is shown -> )
            latinIME.mHandler.postResumeSuggestions(false)
        }
        csv.isGone = true
    }

    companion object {
        private val TAG = "ClipboardHistoryManager"

        // avoid showing the current suggestion because it has been dismissed or pasted
        private var dontShowCurrentSuggestion: Boolean = false

        private fun maySaveFromUri(uri: Uri?, context: Context): Boolean {
            val maxSize = context.prefs().getInt(Settings.PREF_CLIPBOARD_FILES_SIZE_LIMIT, Defaults.PREF_CLIPBOARD_FILES_SIZE_LIMIT)
            val saveUriData = context.prefs().getBoolean(Settings.PREF_CLIPBOARD_USE_FILES, Defaults.PREF_CLIPBOARD_USE_FILES)
            if (uri == null || !saveUriData) return false
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null).use {
                    if (it?.moveToFirst() != true) return false
                    val size = it.getLong(0)
                    return size <= maxSize * 1000000 // maxSize is megabytes
                }
            } catch (e: Exception) {
                Log.w(TAG, "error checking clip size", e) // happens with SecurityException: Permission Denial
                return false
            }
        }
    }
}
