package me.cthorne.kioku.intro;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import me.cthorne.kioku.R;
import me.cthorne.kioku.IntroActivity;

/**
 * Created by chris on 21/02/16.
 */
public class IntroFragment extends Fragment {

    static IntroFragment newInstance(int layout) {
        IntroFragment f = new IntroFragment();

        Bundle args = new Bundle();
        args.putInt("layout", layout);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout = getArguments().getInt("layout");
        View v = inflater.inflate(layout, container, false);

        if (layout == R.layout.activity_intro_screen5) {
            Button gotItButton = (Button)v.findViewById(R.id.got_it_button);
            gotItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntroActivity.activity.finishIntro();
                }
            });
        }

        return v;
    }

}
