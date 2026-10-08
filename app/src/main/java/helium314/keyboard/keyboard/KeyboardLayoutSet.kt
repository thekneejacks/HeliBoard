/*
 * Copyright (C) 2011 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */
package helium314.keyboard.keyboard

import android.content.Context
import android.text.InputType
import android.view.inputmethod.EditorInfo
import helium314.keyboard.keyboard.internal.KeyboardBuilder
import helium314.keyboard.keyboard.internal.KeyboardIconsSet.Companion.needsReload
import helium314.keyboard.keyboard.internal.KeyboardParams
import helium314.keyboard.keyboard.internal.UniqueKeysCache
import helium314.keyboard.keyboard.internal.keyboard_parser.LayoutParser
import helium314.keyboard.keyboard.internal.keyboard_parser.LocaleKeyboardInfos
import helium314.keyboard.latin.RichInputMethodSubtype
import helium314.keyboard.latin.RichInputMethodSubtype.Companion.noLanguageSubtype
import helium314.keyboard.latin.settings.Settings
import helium314.keyboard.latin.utils.InputTypeUtils
import helium314.keyboard.latin.utils.ScriptUtils
import helium314.keyboard.latin.utils.ScriptUtils.script
import helium314.keyboard.latin.utils.SubtypeLocaleUtils.clearSubtypeDisplayNameCache
import java.lang.ref.SoftReference

/**
 * This class represents a set of keyboard layouts. Each of them represents a different keyboard
 * specific to a keyboard state, such as alphabet, symbols, and so on.  Layouts in the same
 * [KeyboardLayoutSet] are related to each other.
 * A [KeyboardLayoutSet] needs to be created for each
 * [EditorInfo].
 */
class KeyboardLayoutSet internal constructor(private val mContext: Context, private val mParams: Params) {
    val script = mParams.script

    /**
     * Represents an internal action that overrides the action provided by the input field.
     * @param code to send on action key press
     * @param label to display on action key
     */
    data class InternalAction(val code: Int, val label: String)

    fun getKeyboard(baseKeyboardLayoutSetElement: KeyboardElement): Keyboard {
        val keyboardLayoutSetElementId = when (mParams.mode) {
            KeyboardMode.PHONE -> {
                if (baseKeyboardLayoutSetElement == KeyboardElement.SYMBOLS) KeyboardElement.PHONE_SYMBOLS
                else KeyboardElement.PHONE
            }
            KeyboardMode.NUMBER, KeyboardMode.DATE, KeyboardMode.TIME, KeyboardMode.DATETIME -> KeyboardElement.NUMBER
            else -> baseKeyboardLayoutSetElement
        }

        // Note: The keyboard for each shift state, and mode are represented as an elementName
        // attribute in a keyboard_layout_set XML file. Also each keyboard layout XML resource is
        // specified as an elementKeyboard attribute in the file.
        // The KeyboardId is an internal key for a Keyboard object.
        val id = KeyboardId(keyboardLayoutSetElementId, mParams)
        try {
            return getKeyboard(id)
        } catch (e: RuntimeException) {
            //Log.e(TAG, "Can't create keyboard: $id", e)
            throw KeyboardLayoutSetException(e)
        }
    }

    private fun getKeyboard(id: KeyboardId): Keyboard {
        val ref = keyboardCache[id]
        val cachedKeyboard = ref?.get()
        if (cachedKeyboard != null) {
            if (DEBUG_CACHE) {
                ////Log.(TAG, "keyboard cache size=${keyboardCache.size}: HIT  id=$id")
            }
            return cachedKeyboard
        }

        val builder = KeyboardBuilder(mContext, KeyboardParams(uniqueKeysCache))
        uniqueKeysCache.setEnabled(id.element.isAlphabet)
        builder.load(id)
        if (mParams.disableTouchPositionCorrectionDataForTest) {
            builder.disableTouchPositionCorrectionDataForTest()
        }
        val keyboard = builder.build()
        keyboardCache[id] = SoftReference<Keyboard>(keyboard)
        if (!mParams.isSpellChecker
            && (id.element == KeyboardElement.ALPHABET || id.element == KeyboardElement.ALPHABET_AUTOMATIC_SHIFTED)
        ) {
            // We only forcibly cache the primary, "ALPHABET", layouts.
            for (i in forcibleKeyboardCache.size - 1 downTo 1) {
                forcibleKeyboardCache[i] = forcibleKeyboardCache[i - 1]
            }
            forcibleKeyboardCache[0] = keyboard
            if (DEBUG_CACHE) {
                ////Log.(TAG, "forcing caching of keyboard with id=$id")
            }
        }
        if (DEBUG_CACHE) {
            ////Log.(TAG, ("keyboard cache size=${keyboardCache.size}: ${(if (ref == null) "LOAD" else "GCed")} id=$id"))
        }
        return keyboard
    }

    class Params {
        var mode = KeyboardMode.TEXT
        var disableTouchPositionCorrectionDataForTest: Boolean = false // remove

        // TODO: Use {@link InputAttributes} instead of these variables.
        lateinit var editorInfo: EditorInfo
        lateinit var subtype: RichInputMethodSubtype
        var voiceInputKeyEnabled = false
        // When the device is still locked, features like showing the IME setting app need to be locked down.
        var deviceLocked = Settings.getValues().mIsLocked
        var numberRowEnabled = false
        var numberRowInSymbols = false
        var languageSwitchKeyEnabled = false
        var isSpellChecker = false
        var keyboardWidth = 0
        var keyboardHeight = 0
        var script = ScriptUtils.SCRIPT_LATIN
        var internalAction: InternalAction? = null
        var emojiSearchAvailable = false

    }

    class Builder(private val mContext: Context, ei: EditorInfo?) {
        private val params = Params()

        init {
            val editorInfo = ei ?: EMPTY_EDITOR_INFO
            params.mode = getKeyboardMode(editorInfo)
            // TODO: Consolidate those with {@link InputAttributes}.
            params.editorInfo = editorInfo
        }

        fun setKeyboardGeometry(keyboardWidth: Int, keyboardHeight: Int): Builder {
            params.keyboardWidth = keyboardWidth
            params.keyboardHeight = keyboardHeight
            return this
        }

        fun setSubtype(subtype: RichInputMethodSubtype): Builder {
            val asciiCapable = subtype.rawSubtype.isAsciiCapable
            val forceAscii = (params.editorInfo.imeOptions and EditorInfo.IME_FLAG_FORCE_ASCII) != 0
            params.subtype = if (forceAscii && !asciiCapable) noLanguageSubtype
                else subtype
            return this
        }

        fun setNumberRowEnabled(enabled: Boolean): Builder {
            params.numberRowEnabled = enabled
            return this
        }

        fun setNumberRowInSymbolsEnabled(enabled: Boolean): Builder {
            params.numberRowInSymbols = enabled
            return this
        }

        fun setLanguageSwitchKeyEnabled(enabled: Boolean): Builder {
            params.languageSwitchKeyEnabled = enabled
            return this
        }

        fun setInternalAction(internalAction: InternalAction?): Builder {
            params.internalAction = internalAction
            return this
        }

        fun build(): KeyboardLayoutSet {
            params.script = params.subtype.locale.script()
            return KeyboardLayoutSet(mContext, params)
        }

        companion object {
            private val EMPTY_EDITOR_INFO = EditorInfo()

            private fun getKeyboardMode(editorInfo: EditorInfo): KeyboardMode {
                val inputType = editorInfo.inputType
                val variation = inputType and InputType.TYPE_MASK_VARIATION

                return when (inputType and InputType.TYPE_MASK_CLASS) {
                    InputType.TYPE_CLASS_NUMBER -> KeyboardMode.NUMBER
                    InputType.TYPE_CLASS_DATETIME -> when (variation) {
                        InputType.TYPE_DATETIME_VARIATION_DATE -> KeyboardMode.DATE
                        InputType.TYPE_DATETIME_VARIATION_TIME -> KeyboardMode.TIME
                        else -> KeyboardMode.DATETIME
                    }
                    InputType.TYPE_CLASS_PHONE -> KeyboardMode.PHONE
                    InputType.TYPE_CLASS_TEXT ->
                        if (InputTypeUtils.isEmailVariation(variation)) KeyboardMode.EMAIL
                        else if (variation == InputType.TYPE_TEXT_VARIATION_URI) KeyboardMode.URL
                        else KeyboardMode.TEXT
                    else -> KeyboardMode.TEXT
                }
            }
        }
    }

    companion object {
        private val TAG = KeyboardLayoutSet::class.java.simpleName
        private const val DEBUG_CACHE = false

        class KeyboardLayoutSetException(cause: Throwable) : RuntimeException(cause)

        // How many layouts we forcibly keep in cache. This only includes ALPHABET (default) and
        // ALPHABET_AUTOMATIC_SHIFTED layouts - other layouts may stay in memory in the map of
        // soft-references, but we forcibly cache this many alphabetic/auto-shifted layouts.
        private const val FORCIBLE_CACHE_SIZE = 4

        // By construction of soft references, anything that is also referenced somewhere else
        // will stay in the cache. So we forcibly keep some references in an array to prevent
        // them from disappearing from sKeyboardCache.
        private val forcibleKeyboardCache = arrayOfNulls<Keyboard>(FORCIBLE_CACHE_SIZE)
        private val keyboardCache = HashMap<KeyboardId, SoftReference<Keyboard>>()
        private val uniqueKeysCache = UniqueKeysCache.newInstance()

        fun onSystemLocaleChanged() {
            clearKeyboardCache()
            LocaleKeyboardInfos.clearCache()
            clearSubtypeDisplayNameCache()
        }

        fun onKeyboardThemeChanged() {
            clearKeyboardCache()
        }

        private fun clearKeyboardCache() {
            keyboardCache.clear()
            uniqueKeysCache.clear()
            LayoutParser.clearCache()
            needsReload = true
        }

    }
}

