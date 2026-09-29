/*
 * Copyright (C) 2015 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.define

/**
 * Decoder specific constants for LatinIme.
 */
object DecoderSpecificConstants {
    // Must be equal to MAX_WORD_LENGTH in native/jni/src/defines.h
    const val DICTIONARY_MAX_WORD_LENGTH = 48

    // (MAX_PREV_WORD_COUNT_FOR_N_GRAM + 1)-gram is supported in Java side. Needs to modify
    // MAX_PREV_WORD_COUNT_FOR_N_GRAM in native/jni/src/defines.h for suggestions.
    const val MAX_PREV_WORD_COUNT_FOR_N_GRAM = 3
}
