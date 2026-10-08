/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */
package helium314.keyboard.latin.utils

import java.util.Locale

/**
 * A class to help with handling different writing scripts.
 */
object ScriptUtils {
    // Unicode scripts (ISO 15924), incomplete
    const val SCRIPT_UNKNOWN = "" // Used for hardware keyboards
    const val SCRIPT_LATIN = "Latn"
    const val SCRIPT_HANGUL = "Hang"

    @JvmStatic
    fun scriptSupportsUppercase(locale: Locale): Boolean {
        // only Latin, Cyrillic, Greek and Armenian have upper/lower case
        // https://unicode.org/faq/casemap_charprop.html#3

        return locale.script() == SCRIPT_LATIN
    }

    /*
     * Returns whether the code point is a letter that makes sense for the specified
     * locale for this spell checker.
     * The dictionaries supported by Latin IME are described in res/xml/spellchecker.xml
     * and is limited to EFIGS languages and Russian.
     * Hence at the moment this explicitly tests for Cyrillic characters or Latin characters
     * as appropriate, and explicitly excludes CJK, Arabic and Hebrew characters.
     */
    @JvmStatic
    fun isLetterPartOfScript(codePoint: Int, script: String): Boolean {
        return when (script) {
            SCRIPT_LATIN ->
                // Our supported latin script dictionaries (EFIGS) at the moment only include
                // characters in the C0, C1, Latin Extended A and B, IPA extensions unicode
                // blocks. As it happens, those are back-to-back in the code range 0x40 to 0x2AF,
                // so the below is a very efficient way to test for it. As for the 0-0x3F, it's
                // excluded from isLetter anyway.
                codePoint <= 0x2AF && Character.isLetter(codePoint)
            SCRIPT_HANGUL -> codePoint in 0xAC00..0xD7A3
                    || codePoint in 0x3131..0x318E
                    || codePoint in 0x1100..0x11FF
                    || codePoint in 0xA960..0xA97C
                    || codePoint in 0xD7B0..0xD7C6
                    || codePoint in 0xD7CB..0xD7FB
            SCRIPT_UNKNOWN -> true
            else -> throw RuntimeException("Unknown value of script: $script")
        }
    }

    /**
     * returns the locale script with fallback to default scripts
     */
    @JvmStatic
    fun Locale.script(): String {
        if (script.isNotEmpty()) return script
        if (country.equals("ZZ", true)) {
            //Log.w("ScriptUtils", "old _ZZ locale found: $this")
            return SCRIPT_LATIN
        }

        if (language == "ko"){
            return SCRIPT_HANGUL
        }
        else return SCRIPT_LATIN
    }

    @JvmStatic
    fun isScriptRtl(script: String): Boolean {
        return false
    }
}
