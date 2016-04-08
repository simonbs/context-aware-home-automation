package aau.carma;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import java.util.ArrayList;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridViewPager mGridPager = (GridViewPager) findViewById(R.id.main_pager);
        mGridPager.setAdapter(new GridPagerAdapter(getFragmentManager()));
    }

    /**
     * Adapter managing pages in the grid pager.
     */
    private class GridPagerAdapter extends FragmentGridPagerAdapter {
        /**
         * Rows in the grid pager.
         */
        private ArrayList<PagesRow> rows;

        /**
         * Instantiates an adapter managing the pages.
         * @param fragmentManager Manages fragments.
         */
        public GridPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            setupPages();
        }

        /**
         * Setup the pages in the grid pager.
         */
        private void setupPages() {
            rows = new ArrayList<>();
            PagesRow row1 = new PagesRow();
            row1.addPage(Page.RECOGNIZE_GESTURE);
            row1.addPage(Page.SETTINGS);
            rows.add(row1);
        }

        @Override
        public Fragment getFragment(int row, int col) {
            Page page = rows.get(row).getPage(col);
            switch (page) {
                case RECOGNIZE_GESTURE:
                    return new RecognizeGestureFragment();
                case SETTINGS:
                    return new SettingsFragment();
            }

            return null;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount(int row) {
            return rows.get(row).size();
        }
    }

    /**
     * Represents a row of pages.
     */
    private class PagesRow {
        /**
         * Holds pages in the row.
         */
        ArrayList<Page> pages = new ArrayList<>();

        /**
         * Add a page to the row.
         * @param page Page to add.
         */
        public void addPage(Page page) {
            pages.add(page);
        }

        /**
         * Get the page at the specified index.
         * @param index Index of page.
         * @return Page at the specified index.
         */
        public Page getPage(int index) {
            return pages.get(index);
        }

        /**
         * Amount of pages in the row.
         * @return Pages in the row.
         */
        public int size() {
            return pages.size();
        }
    }

    /**
     * Pages in the grid pager.
     */
    private enum Page {
        RECOGNIZE_GESTURE,
        SETTINGS
    }
}
