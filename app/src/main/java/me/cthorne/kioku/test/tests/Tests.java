package me.cthorne.kioku.test.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.cthorne.kioku.DatabaseHelper;
import me.cthorne.kioku.MainActivity;
import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestType;
import me.cthorne.kioku.words.WordInformationType;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 07/01/16.
 */
public class Tests {

    private static Map<WordInformationTestType, ArrayList<WordInformationTest>> tests;
    private static WordLanguage loadedTestsLanguage;
    private static WordInformationTest activeTest;
    private static WordInformationTestType activeTestType;
    private static int currentTestIndex = 0;

    public static void load() {
        tests = new HashMap<>();

        ArrayList<WordInformationTest> vocabularyComprehensionTests = new ArrayList<>();
        vocabularyComprehensionTests.add(new VocabularyComprehensionQuadTest());
        vocabularyComprehensionTests.add(new VocabularyComprehensionMatchTest());
        tests.put(WordInformationTestType.VOCABULARY_COMPREHENSION, vocabularyComprehensionTests);

        ArrayList<WordInformationTest> vocabularyRecallTests = new ArrayList<>();
        vocabularyRecallTests.add(new TranslationToWordQuadTest());
        vocabularyRecallTests.add(new TranslationToWordMatchTest());
        vocabularyRecallTests.add(new ImageToWordQuadTest());
        vocabularyRecallTests.add(new ImageToWordMatchTest());
        //vocabularyRecallTests.add(new DefinitionToWordQuadTest());
        //vocabularyRecallTests.add(new DefinitionToWordMatchTest());
        tests.put(WordInformationTestType.VOCABULARY_RECALL, vocabularyRecallTests);

        ArrayList<WordInformationTest> sentenceComprehensionTests = new ArrayList<>();
        sentenceComprehensionTests.add(new SentenceQuadTest());
        sentenceComprehensionTests.add(new SentenceMatchTest());
        tests.put(WordInformationTestType.SENTENCE_COMPREHENSION, sentenceComprehensionTests);

        // Language specific tests

        if (MainActivity.currentLanguage == WordLanguage.JP) {
            ArrayList<WordInformationTest> kanjiWritingTests = new ArrayList<>();
            kanjiWritingTests.add(new KanjiWritingQuadTest());
            kanjiWritingTests.add(new KanjiWritingMatchTest());
            tests.put(WordInformationTestType.KANJI_WRITING, kanjiWritingTests);

            ArrayList<WordInformationTest> kanjiReadingTests = new ArrayList<>();
            kanjiReadingTests.add(new KanjiToKanaQuadTest());
            kanjiReadingTests.add(new KanjiToKanaMatchTest());
            tests.put(WordInformationTestType.KANJI_READING, kanjiReadingTests);
        }

        loadedTestsLanguage = MainActivity.currentLanguage;
    }

    public static WordInformationTest getActiveTest() {
        List<WordInformationTest> testTypeTests = getAll().get(activeTestType);

        if (currentTestIndex >= testTypeTests.size())
            return null;

        return (activeTest = testTypeTests.get(currentTestIndex));
    }

    public static void proceedToNextTest(DatabaseHelper dbHelper) {
        List<WordInformationTest> testTypeTests = getAll().get(activeTestType);

        // Search all test types for the earliest possible test that isn't the current one
        for (int i = 0; i < testTypeTests.size(); i++) {
            if (testTypeTests.get(i).canStartTest(dbHelper) && currentTestIndex != i) {
                currentTestIndex = i;
                return;
            }
        }

        // Otherwise, there are no tests to do
        currentTestIndex = Integer.MAX_VALUE;
    }

    /**
     * Checks if there is an easier test than the current test for the current test type.
     * @return
     */
    public static boolean hasEasierTestThanCurrent(DatabaseHelper dbHelper) {
        List<WordInformationTest> testTypeTests = getAll().get(activeTestType);

        // Check all tests following current one to see if there's an easier test present
        for (int i = currentTestIndex+1; i < testTypeTests.size(); i++) {
            if (activeTest.getTestWordInformationType() == testTypeTests.get(i).getTestWordInformationType() && testTypeTests.get(i).canStartTest(dbHelper)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasTestAfterCurrent(DatabaseHelper dbHelper) {
        List<WordInformationTest> testTypeTests = getAll().get(activeTestType);

        // Check all tests following current one to see if any are available
        //for (int i = currentTestIndex+1; i < testTypeTests.size(); i++) {
        for (int i = 0; i < testTypeTests.size(); i++) {
            if (testTypeTests.get(i).canStartTest(dbHelper)) {
                return true;
            }
        }

        return false;
    }

    public static void resetCurrentTestIndex() {
        currentTestIndex = 0;
    }

    public static WordInformationTestType getActiveTestType() {
        return activeTestType;
    }

    public static void setActiveTestType(WordInformationTestType testType) {
        activeTestType = testType;
        resetCurrentTestIndex();
    }

    public static Map<WordInformationTestType, ArrayList<WordInformationTest>> getAll() {
        if (tests == null || loadedTestsLanguage != MainActivity.currentLanguage)
            load();

        return tests;
    }

    public static long countDueForTestType(WordInformationTestType testType, DatabaseHelper dbHelper) {
        long total = 0;

        Map<WordInformationType, Long> maxForInformationType = new HashMap<>();

        for (WordInformationTest test : getAll().get(testType)) {
            long count = test.countNumberOfTests(dbHelper);
            long maxCount = maxForInformationType.containsKey(test.getTestWordInformationType()) ?
                    maxForInformationType.get(test.getTestWordInformationType()) : 0;

            maxCount = Math.max(count, maxCount);

            maxForInformationType.put(test.getTestWordInformationType(), maxCount);
        }

        for (Long max : maxForInformationType.values())
            total += max;

        return total;
    }

    /**
     * Calculates a count for the number of word informations each test type is due to show and totals them up.
     * If there are multiple tests of the same test type, the larger count of the group is used.
     * @param dbHelper
     * @return
     */
    public static long countDueTests(DatabaseHelper dbHelper) {
        Map<WordInformationTestType, Long> testTypeCounts = new HashMap<>();

        // Get counts for each test type

        for (WordInformationTestType testType : getAll().keySet())
            testTypeCounts.put(testType, countDueForTestType(testType, dbHelper));

        // Sum counts for each test type

        long total = 0;
        for (Long count : testTypeCounts.values())
            total += count;

        return total;
    }

    /**
     * Checks if the given test type is possible to start by going through all possible tests for the type
     * and seeing if the test can be started.
     * @param dbHelper
     * @return
     */
    public static boolean canStartTestType(WordInformationTestType testType, DatabaseHelper dbHelper) {
        for (WordInformationTest test : getAll().get(testType)) {
            if (test.canStartTest(dbHelper)) {
                return true;
            }
        }

        return false;
    }
}
