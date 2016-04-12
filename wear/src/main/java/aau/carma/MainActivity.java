package aau.carma;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;

import aau.carma.GridPager.GridAdapter;
import aau.carma.GridPager.GridFragmentProvider;
import aau.carma.GridPager.GridRow;
import aau.carma.GridPager.GridViewPager;

public class MainActivity extends Activity implements GridFragmentProvider<MainActivity.Page>, RecognizeGestureFragment.RecognitionListener {
    /**
     * Grid pager managing the pages.
     */
    private GridViewPager gridPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridRow<Page> gridRow = new GridRow<>();
        gridRow.addPage(Page.RECOGNIZE_GESTURE);
        gridRow.addPage(Page.SETTINGS);

        ArrayList<GridRow<Page>> gridRows = new ArrayList<>();
        gridRows.add(gridRow);

        GridAdapter<Page> gridAdapter = new GridAdapter<>(getFragmentManager());
        gridAdapter.setRows(gridRows);
        gridAdapter.setFragmentProvider(this);

        gridPager = (GridViewPager) findViewById(R.id.main_pager);
        gridPager.setAdapter(gridAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    public Fragment fragmentForPage(Page page) {
        switch (page) {
            case RECOGNIZE_GESTURE:
                RecognizeGestureFragment fragment = new RecognizeGestureFragment();
                fragment.setRecognitionListener(this);
                return fragment;
            case SETTINGS:
                return new SettingsFragment();
        }

        return null;
    }

    @Override
    public void onBeginRecognizing() {
        gridPager.setScrollEnabled(false);
    }

    @Override
    public void onEndRecognizing() {
        gridPager.setScrollEnabled(true);
    }

    /**
     * Pages in the grid pager.
     */
    public enum Page {
        RECOGNIZE_GESTURE,
        SETTINGS
    }
}
