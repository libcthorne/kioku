package me.cthorne.kioku.test;

/**
 * Created by chris on 07/01/16.
 */
public enum WordInformationTestType {

    UNKNOWN,
    VOCABULARY_RECALL,
    VOCABULARY_COMPREHENSION,
    SENTENCE_COMPREHENSION,
    KANJI_READING,
    KANJI_WRITING,
    SPELLING,
    PRONUNCIATION;

    public String toName() {
        switch (this) {
            case UNKNOWN: return "Unknown";
            case VOCABULARY_RECALL: return "Vocabulary Recall";
            case VOCABULARY_COMPREHENSION: return "Vocabulary Comprehension";
            case SENTENCE_COMPREHENSION: return "Sentence Comprehension";
            case KANJI_READING: return "Kanji Reading";
            case KANJI_WRITING: return "Kanji Writing";
            case SPELLING: return "Spelling";
            case PRONUNCIATION: return "Pronunciation";
            default: return super.toString();
        }
    }
}
