package aau.carma.Picker;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import aau.carma.R;

/**
 * View for a setting in the settings fragment.
 */
public final class WearableListItemView extends FrameLayout implements WearableListView.OnCenterProximityListener {
    /**
     * Shows the icon
     */
    final ImageView iconImageView;

    /**
     * Shows the title.
     */
    final TextView titleTextView;

    public WearableListItemView(Context context) {
        super(context);
        View.inflate(context, R.layout.wearable_list_item, this);
        iconImageView = (ImageView)findViewById(R.id.wearable_list_item_icon);
        titleTextView = (TextView)findViewById(R.id.wearable_list_item_title);
    }

    @Override
    public void onCenterPosition(boolean b) {
        iconImageView.animate().scaleX(1f).scaleY(1f).alpha(1);
        titleTextView.animate().scaleX(1f).scaleY(1f).alpha(1);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        iconImageView.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        titleTextView.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
    }
}