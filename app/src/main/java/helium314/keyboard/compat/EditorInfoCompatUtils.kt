/*
 * Copyright (C) 2011 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.compat

import android.os.Build
import android.view.inputmethod.EditorInfo
import java.util.Locale

object EditorInfoCompatUtils {


    @JvmStatic
    fun getHintLocales(editorInfo: EditorInfo?): List<Locale> {
        if (editorInfo == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return listOf()
        }
        val localeList = editorInfo.hintLocales ?: return listOf()
        val locales = ArrayList<Locale>(localeList.size())
        for (i in 0 until localeList.size()) {
            locales.add(localeList.get(i))
        }
        return locales
    }
}
