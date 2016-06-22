package me.cthorne.kioku.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

import me.cthorne.kioku.R;
import me.cthorne.kioku.infosources.SourcesActivity;
import me.cthorne.kioku.words.WordLanguage;

/**
 * Created by chris on 23/01/16.
 */
public class LanguageSettingsActivity extends AppCompatActivity {

    private static WordLanguage language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = WordLanguage.fromInt(getIntent().getIntExtra("language", -1));

        setTitle(language.toString());

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LanguageSettingsFragment())
                .commit();
    }

    public static class LanguageSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_language);

            final Context context = getActivity();

            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("preferenceScreen");

            Preference prefWordSources = preferenceScreen.findPreference("pref_word_sources");
            prefWordSources.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent wordInformationSourcesIntent = new Intent(context, SourcesActivity.class);
                    wordInformationSourcesIntent.putExtra("language", language.getValue());
                    startActivity(wordInformationSourcesIntent);

                    return false;

                }
            });
        }
    }
}
