/*
 * Copyright (C) 2013 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a range of text, relative to the current cursor position.
 */
public final class TextRange {
    private final CharSequence mTextAtCursor;
    private final int mWordAtCursorStartIndex;
    private final int mWordAtCursorEndIndex;
    private final int mCursorIndex;

    public final CharSequence mWord;
    public final boolean mHasUrlSpans;

    public int getNumberOfCharsInWordBeforeCursor() {
        return mCursorIndex - mWordAtCursorStartIndex;
    }

    public int getNumberOfCharsInWordAfterCursor() {
        return mWordAtCursorEndIndex - mCursorIndex;
    }

    public int length() {
        return mWord.length();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TextRange textRange)) return false;
        return mWordAtCursorStartIndex == textRange.mWordAtCursorStartIndex
            && mWordAtCursorEndIndex == textRange.mWordAtCursorEndIndex
            && mCursorIndex == textRange.mCursorIndex
            && mHasUrlSpans == textRange.mHasUrlSpans
            && TextUtils.equals(mTextAtCursor, textRange.mTextAtCursor)
            && TextUtils.equals(mWord, textRange.mWord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTextAtCursor, mWordAtCursorStartIndex, mWordAtCursorEndIndex, mCursorIndex, mWord, mHasUrlSpans);
    }

    @NonNull
    @Override
    public String toString() {
        return mTextAtCursor + ", " + mWord + ", " + mCursorIndex;
    }

    public TextRange(final CharSequence textAtCursor, final int wordAtCursorStartIndex,
            final int wordAtCursorEndIndex, final int cursorIndex, final boolean hasUrlSpans) {
        if (wordAtCursorStartIndex < 0 || cursorIndex < wordAtCursorStartIndex
                || cursorIndex > wordAtCursorEndIndex
                || wordAtCursorEndIndex > textAtCursor.length()) {
            throw new IndexOutOfBoundsException();
        }
        mTextAtCursor = textAtCursor;
        mWordAtCursorStartIndex = wordAtCursorStartIndex;
        mWordAtCursorEndIndex = wordAtCursorEndIndex;
        mCursorIndex = cursorIndex;
        mHasUrlSpans = hasUrlSpans;
        mWord = mTextAtCursor.subSequence(mWordAtCursorStartIndex, mWordAtCursorEndIndex);
    }
}
