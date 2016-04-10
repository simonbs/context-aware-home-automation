package aau.carma.GridPager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.util.ArrayList;

import aau.carmakit.Utilities.Optional;

/**
 * Adapter managing pages in the grid pager.
 * @param <T> Type of the page.
 */
public class GridAdapter<T> extends FragmentGridPagerAdapter {
    /**
     * Rows in the grid pager.
     */
    private ArrayList<GridRow<T>> rows;

    /**
     * Object providing the fragments.
     */
    private Optional<GridFragmentProvider<T>> fragmentProvider;

    /**
     * Instantiates an adapter managing the pages.
     * @param fragmentManager Manages fragments.
     */
    public GridAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Sets the object providing fragments for the grid.
     * @param gridFragmentProvider Object providing fragments for the grid.
     */
    public void setFragmentProvider(GridFragmentProvider<T> gridFragmentProvider) {
        this.fragmentProvider = new Optional<>(gridFragmentProvider);
    }

    /**
     * Set the rows to display in the grid.
     * @param rows Rows to display in the grid.
     */
    public void setRows(ArrayList<GridRow<T>> rows) {
        this.rows = rows;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (fragmentProvider.isPresent()) {
            T page = rows.get(row).getPage(col);
            return fragmentProvider.value.fragmentForPage(page);
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
