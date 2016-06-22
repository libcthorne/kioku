package me.cthorne.kioku.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import me.cthorne.kioku.infosources.WordInformationSource;

public class SearchWebViewFragmentAdapter extends FragmentPagerAdapter {
    List<WordInformationSource> sources;
    String searchString;
    HashMap<Integer, SearchWebViewFragment> savedFragments = new HashMap<>();

    public SearchWebViewFragmentAdapter(FragmentManager fm, String searchString, List<WordInformationSource> sources) {
        super(fm);

        this.searchString = searchString;
        this.sources = sources;
    }

    @Override
    public int getCount() {
        return sources.size();
    }

    @Override
    public Fragment getItem(int position) {
        WordInformationSource source = sources.get(position);
        String url = source.getUrl(searchString);
        String selectJS = source.getSelectJS();
        String saveJS = source.getSaveJS();

        Log.d("kioku-search", "select test: " + selectJS);

        return SearchWebViewFragment.newInstance(url, selectJS, saveJS);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = sources.get(position).getTitle();

        return title;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);

        savedFragments.put(position, (SearchWebViewFragment)fragment);

        return fragment;
    }

    public HashMap<Integer, SearchWebViewFragment> getSavedWebViewFragments() {
        return savedFragments;
    }

    public SearchWebViewFragment getSavedWebViewFragment(int position) {
        return savedFragments.get(position);
    }
}