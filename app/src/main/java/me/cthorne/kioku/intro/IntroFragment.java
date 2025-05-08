package me.cthorne.kioku.intro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.Button;

import me.cthorne.kioku.R;
import me.cthorne.kioku.IntroActivity;

/**
 * Created by chris on 23/01/16.
 */
public class IntroFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static IntroFragment newInstance(int sectionNumber) {
        IntroFragment fragment = new IntroFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public IntroFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layoutResource = getArguments().getInt(ARG_SECTION_NUMBER);
        View rootView = inflater.inflate(layoutResource, container, false);

        if (getArguments().getInt(ARG_SECTION_NUMBER) == R.layout.activity_intro_screen5) {
            Button gotItButton = (Button)rootView.findViewById(R.id.got_it_button);
            gotItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntroActivity.activity.finishIntro();
                }
            });
        }

        return rootView;
    }
}
