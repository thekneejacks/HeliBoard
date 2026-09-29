/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.android.inputmethod.latin.utils;

import helium314.keyboard.latin.NgramContext;
import helium314.keyboard.latin.common.StringUtils;
import helium314.keyboard.latin.define.DecoderSpecificConstants;

// Note: this class is used as a parameter type of a native method. You should be careful when you
// rename this class or field name. See BinaryDictionary#addMultipleDictionaryEntriesNative().
public final class WordInputEventForPersonalization {
    private static final String TAG = WordInputEventForPersonalization.class.getSimpleName();

    public final int[] mTargetWord;
    public final int mPrevWordsCount;
    public final int[][] mPrevWordArray =
            new int[DecoderSpecificConstants.MAX_PREV_WORD_COUNT_FOR_N_GRAM][];
    public final boolean[] mIsPrevWordBeginningOfSentenceArray =
            new boolean[DecoderSpecificConstants.MAX_PREV_WORD_COUNT_FOR_N_GRAM];
    // Time stamp in seconds.
    public final int mTimestamp;

    public WordInputEventForPersonalization(final CharSequence targetWord,
            final NgramContext ngramContext, final int timestamp) {
        mTargetWord = StringUtils.toCodePointArray(targetWord);
        mPrevWordsCount = ngramContext.getPrevWordCount();
        ngramContext.outputToArray(mPrevWordArray, mIsPrevWordBeginningOfSentenceArray);
        mTimestamp = timestamp;
    }

}
