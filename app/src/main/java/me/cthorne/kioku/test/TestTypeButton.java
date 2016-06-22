package me.cthorne.kioku.test;

import android.widget.Button;

/**
 * Created by chris on 12/01/16.
 */
public class TestTypeButton {

    private Button button;
    private WordInformationTestType testType;

    public TestTypeButton(Button button, WordInformationTestType testType) {
        this.button = button;
        this.testType = testType;
    }

    public Button getButton() {
        return button;
    }

    public WordInformationTestType getTestType() {
        return testType;
    }

}
