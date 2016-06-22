package me.cthorne.kioku.words;

/**
 * Created by chris on 12/01/16.
 */
public enum WordInformationType {
    UNKNOWN,
    WORD_FORM, // 犬 [metainfo: furigana/reading, e.g. いぬ] (words with no kanji, e.g. イギリス, only use metainfo)
    TRANSLATION, // dog
    DEFINITION, // 犬とは…
    SENTENCE, // 私は犬が飼いたいです [metainfo: translation, e.g. I want a pet dog.]
    IMAGE, // Picture of a dog
    AUDIO, // Pronunciation of "犬" in Japanese
    NOTES // User notes (e.g. "This word is only used in formal situations")
}