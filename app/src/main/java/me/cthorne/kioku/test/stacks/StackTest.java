package me.cthorne.kioku.test.stacks;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import me.cthorne.kioku.test.WordInformationTest;
import me.cthorne.kioku.test.WordInformationTestActivity;
import me.cthorne.kioku.test.tests.Tests;

/**
 * Created by chris on 08/02/16.
 */
public abstract class StackTest extends WordInformationTest {

    protected StackFragmentAdapter adapter;

    private void cleanupFragments() {
        AppCompatActivity activity = (AppCompatActivity)getTestActivity();

        if (activity.isFinishing()) {
            Log.d("kioku-stack", "activity is finishing; skip cleanup");
            return;
        }

        Log.d("kioku-stack", "cleanup fragments");

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : adapter.getSavedFragments().values()) {
            transaction.remove(fragment);
        }
        transaction.commit();
    }

    @Override
    public void resetTest() {
        cleanupFragments();
    }

    @Override
    public void finishTest() {
        // Remove current fragments if another test is going to be using this activity
        if (Tests.hasTestAfterCurrent(WordInformationTest.activity.getHelper()))
            cleanupFragments();

        WordInformationTestActivity.currentWordInformation = null;

        // Super call changes layout and so should be called last
        super.finishTest();
    }
}
