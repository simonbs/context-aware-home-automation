package aau.carma.GridPager;

import android.app.Fragment;

/**
 * Implemented by objects that provides pages for the grid.
 * @param <T> Type of the page.
 */
public interface GridFragmentProvider<T> {
    Fragment fragmentForPage(T page);
}