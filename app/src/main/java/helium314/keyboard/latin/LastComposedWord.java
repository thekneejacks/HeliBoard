/*
 * Copyright (C) 2012 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin;

import java.util.ArrayList;

import helium314.keyboard.event.Event;
import helium314.keyboard.latin.common.InputPointers;
import helium314.keyboard.latin.define.DecoderSpecificConstants;

/**
 * This class encapsulates data about a word previously composed, but that has been
 * committed already. This is used for resuming suggestion, and cancel auto-correction.
 */
public final class LastComposedWord {
    // COMMIT_TYPE_USER_TYPED_WORD is used when the word committed is the exact typed word, with
    // no hinting from the IME. It happens when some external event happens (rotating the device,
    // for example) or when auto-correction is off by settings or editor attributes.
    public static final int COMMIT_TYPE_USER_TYPED_WORD = 0;

    public static final String NOT_A_SEPARATOR = "";

    public final ArrayList<Event> mEvents;
    public final String mTypedWord;
    public final CharSequence mCommittedWord;
    public final String mSeparatorString;
    public final CapsMode mCapitalizedMode;
    public final InputPointers mInputPointers =
        new InputPointers(DecoderSpecificConstants.DICTIONARY_MAX_WORD_LENGTH);

    public static final LastComposedWord NOT_A_COMPOSED_WORD =
        new LastComposedWord(new ArrayList<Event>(), null, "", "",
            NOT_A_SEPARATOR,  CapsMode.OFF);

    // Warning: this is using the passed objects as is and fully expects them to be
    // immutable. Do not fiddle with their contents after you passed them to this constructor.
    public LastComposedWord(ArrayList<Event> events, InputPointers inputPointers, String typedWord,
                            CharSequence committedWord, String separatorString, CapsMode capitalizedMode) {
        if (inputPointers != null) {
            mInputPointers.copy(inputPointers);
        }
        mTypedWord = typedWord;
        mEvents = new ArrayList<>(events);
        mCommittedWord = committedWord;
        mSeparatorString = separatorString;
        mCapitalizedMode = capitalizedMode;
    }
}
