/*
 * Copyright (C) 2008 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.dictionary;

import java.util.Locale;

import helium314.keyboard.latin.makedict.WordProperty;

/**
 * Abstract base class for a dictionary that can do a fuzzy search for words based on a set of key
 * strokes.
 */
public abstract class Dictionary {
    public static final int NOT_A_PROBABILITY = -1;
    public static final float NOT_A_WEIGHT_OF_LANG_MODEL_VS_SPATIAL_MODEL = -1.0f;

    // The following types do not actually come from real dictionary instances, so we create
    // corresponding instances.
    public static final String TYPE_USER_TYPED = "user_typed";
    public static final PhonyDictionary DICTIONARY_USER_TYPED = new PhonyDictionary(TYPE_USER_TYPED);

    public static final String TYPE_USER_SHORTCUT = "user_shortcut";
    public static final PhonyDictionary DICTIONARY_USER_SHORTCUT =
            new PhonyDictionary(TYPE_USER_SHORTCUT);

    public static final String TYPE_APPLICATION_DEFINED = "application_defined";
    public static final PhonyDictionary DICTIONARY_APPLICATION_DEFINED =
            new PhonyDictionary(TYPE_APPLICATION_DEFINED);

    public static final String TYPE_HARDCODED = "hardcoded"; // punctuation signs and such
    public static final PhonyDictionary DICTIONARY_HARDCODED =
            new PhonyDictionary(TYPE_HARDCODED);

    // Spawned by resuming suggestions. Comes from a span that was in the TextView.
    public static final String TYPE_RESUMED = "resumed";
    public static final PhonyDictionary DICTIONARY_RESUMED = new PhonyDictionary(TYPE_RESUMED);

    // The following types of dictionary have actual functional instances. We don't need final
    // phony dictionary instances for them.
    public static final String TYPE_MAIN = "main";
    public static final String TYPE_CONTACTS = "contacts";
    public static final String TYPE_APPS = "apps";
    // User dictionary, the system-managed one.
    public static final String TYPE_USER = "user";
    // User history dictionary internal to LatinIME.
    public static final String TYPE_USER_HISTORY = "history";
    public static final String TYPE_EMOJI = "emoji";
    public final String mDictType;
    // The locale for this dictionary. May be null if unknown (phony dictionary for example).
    public final Locale mLocale;

    public Dictionary(final String dictType, final Locale locale) {
        mDictType = dictType;
        mLocale = locale;
    }

    /**
     * Checks if the given word has to be treated as a valid word. Please note that some
     * dictionaries have entries that should be treated as invalid words.
     * @param word the word to search for. The search should be case-insensitive.
     * @return true if the word is valid, false otherwise
     */
    public boolean isValidWord(final String word) {
        return isInDictionary(word);
    }

    /**
     * Checks if the given word is in the dictionary regardless of it being valid or not.
     */
    abstract public boolean isInDictionary(final String word);

    /**
     * Get the frequency of the word.
     * @param word the word to get the frequency of.
     */
    public int getFrequency(final String word) {
        return NOT_A_PROBABILITY;
    }

    /**
     * Get the maximum frequency of the word.
     * @param word the word to get the maximum frequency of.
     */
    public int getMaxFrequencyOfExactMatches(final String word) {
        return NOT_A_PROBABILITY;
    }

    /**
     * Compares the contents of the character array with the typed word and returns true if they
     * are the same.
     * @param word the array of characters that make up the word
     * @param length the number of valid characters in the character array
     * @param typedWord the word to compare with
     * @return true if they are the same, false otherwise.
     */
    protected boolean same(final char[] word, final int length, final String typedWord) {
        if (typedWord.length() != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (word[i] != typedWord.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Override to clean up any resources.
     */
    public void close() {
        // empty base implementation
    }

    public void onFinishInput() {
        //empty base implementation
    }

    /**
     * Subclasses may override to indicate that this Dictionary is not yet properly initialized.
     */
    public boolean isInitialized() {
        return true;
    }

    /**
     * Whether this dictionary is based on data specific to the user, e.g., the user's contacts.
     * @return Whether this dictionary is specific to the user.
     */
    public boolean isUserSpecific() {
        return switch (mDictType) {
            case TYPE_USER_TYPED,
                 TYPE_USER,
                 TYPE_CONTACTS,
                 TYPE_APPS,
                 TYPE_USER_HISTORY -> true;
            default -> false;
        };
    }

    public WordProperty getWordProperty(final String word, final boolean isBeginningOfSentence) {
        return null;
    }

    /**
     * Not a true dictionary. A placeholder used to indicate suggestions that don't come from any
     * real dictionary.
     */
    public static class PhonyDictionary extends Dictionary {
        public PhonyDictionary(final String type) {
            super(type, null);
        }

        @Override
        public boolean isInDictionary(String word) {
            return false;
        }
    }
}
