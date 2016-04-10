package aau.carma.GridPager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import aau.carmakit.Utilities.Logger;

/**
 * Grid view pager allowing disable of scroll.
 */
public class GridViewPager extends android.support.wearable.view.GridViewPager {
    /**
     * Whether or not scroll is enabled.
     */
    private boolean isScrollEnabled = true;

    public GridViewPager(Context context) {
        super(context);
    }

    public GridViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Enable or disable scrolling in the grid view pager.
     * @param enabled Whether or not scroll should be enabled.
     */
    public void setScrollEnabled(boolean enabled) {
        isScrollEnabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE && !isScrollEnabled) {
            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
