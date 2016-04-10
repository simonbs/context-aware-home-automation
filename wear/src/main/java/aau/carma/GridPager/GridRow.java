package aau.carma.GridPager;

import java.util.ArrayList;

/**
 * Represents a row of pages.
 * @param <T> Type of the pages.
 */
public class GridRow<T> {
    /**
     * Holds pages in the row.
     */
    private ArrayList<T> pages = new ArrayList<>();

    /**
     * Add a page to the row.
     * @param page Page to add.
     */
    public void addPage(T page) {
        pages.add(page);
    }

    /**
     * Get the page at the specified index.
     * @param index Index of page.
     * @return Page at the specified index.
     */
    public T getPage(int index) {
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