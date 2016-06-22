package me.cthorne.kioku;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.viewpagerindicator.CirclePageIndicator;

import me.cthorne.kioku.intro.IntroFragmentAdapter;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by chris on 21/02/16.
 */
public class IntroActivity extends AppCompatActivity {

    public static IntroActivity activity;

    private boolean manualStart; // user came here voluntarily
    private boolean lastPage; // user has seen the last page of the tutorial

    private RelativeLayout leftButton;
    private RelativeLayout rightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        IntroActivity.activity = this;

        manualStart = getIntent().getBooleanExtra("manualStart", false);

        if (manualStart)
            lastPage = true; // just show "ok" instead of "skip" if the user came here manually

        final ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        final IntroFragmentAdapter adapter = new IntroFragmentAdapter(getSupportFragmentManager());

        leftButton = (RelativeLayout)findViewById(R.id.left_button);
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
                return true;
            }
        });
        leftButton.setVisibility(View.INVISIBLE); // hide on first page

        rightButton = (RelativeLayout)findViewById(R.id.right_button);
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                return true;
            }
        });

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Change "skip" to "ok" in menu
                if (position == adapter.getCount()-1) {
                    lastPage = true;
                    invalidateOptionsMenu();
                }

                // Left/right button visibility
                if (position == 0) {
                    leftButton.setVisibility(View.INVISIBLE);
                    rightButton.setVisibility(View.VISIBLE);
                } else if (position == adapter.getCount()-1) {
                    leftButton.setVisibility(View.VISIBLE);
                    rightButton.setVisibility(View.INVISIBLE);
                } else {
                    leftButton.setVisibility(View.VISIBLE);
                    rightButton.setVisibility(View.VISIBLE);
                }

                // Reset gif playback
                if (position >= 1 && position <= 4) {
                    int containerId;
                    switch (position) {
                        case 1:
                            containerId = R.id.tutorial_screen_step1;
                            break;
                        case 2:
                            containerId = R.id.tutorial_screen_step2;
                            break;
                        case 3:
                            containerId = R.id.tutorial_screen_step3;
                            break;
                        case 4:
                            containerId = R.id.tutorial_screen_step4;
                            break;
                        default:
                            return;
                    }

                    LinearLayout container = (LinearLayout) findViewById(containerId);
                    GifImageView gifImageView = (GifImageView) container.findViewById(R.id.step_gif);
                    GifDrawable gifDrawable = (GifDrawable)gifImageView.getDrawable();
                    gifDrawable.setSpeed(1.3f);
                    gifDrawable.reset();
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        CirclePageIndicator progressIndicator = (CirclePageIndicator)findViewById(R.id.progress);
        progressIndicator.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(lastPage ? R.menu.menu_intro_ok : R.menu.menu_intro_skip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.skip_button:
            case R.id.ok_button:
                finishIntro();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void finishIntro() {
        if (manualStart) {
            finish();
        } else {
            KiokuServerClient.getPreferences(this).edit().putBoolean("seenIntro", true).commit();
            Intent intent = new Intent(this, LoaderActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
