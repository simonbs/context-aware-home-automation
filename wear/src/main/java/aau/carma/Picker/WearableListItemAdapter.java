package aau.carma.Picker;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import aau.carma.R;
import aau.carmakit.Utilities.Optional;

/**
 * Adapter managing items in a wearable list view.
 */
public class WearableListItemAdapter extends WearableListView.Adapter {
    /**
     * Context used for configuring views.
     */
    private final Context context;

    /**
     * Displayed items.
     */
    private final ArrayList<WearableListItem> items;

    /**
     * Initializes an adapter for managing items in a wearable list view.
     * @param context Context the adapter is used in.
     * @param items Items to show in the list view.
     */
    public WearableListItemAdapter(Context context, ArrayList<WearableListItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, final int position) {
        WearableListItemView itemView = (WearableListItemView) viewHolder.itemView;
        final WearableListItem item = items.get(position);

        if (item.getTitle().isPresent()) {
            TextView textView = (TextView) itemView.findViewById(R.id.wearable_list_item_title);
            textView.setText(item.getTitle().value);
        }

        if (item.getIconResource().isPresent()) {
            final ImageView imageView = (ImageView) itemView.findViewById(R.id.wearable_list_item_icon);
            imageView.setImageResource(item.getIconResource().value);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(new WearableListItemView(context));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Gets all items in the adapter.
     * @return Items in the adapter.
     */
    public Optional<ArrayList<WearableListItem>> getItems() {
        if (items == null) {
            return new Optional<>();
        }

        return new Optional<>(items);
    }

    /**
     * Gets the item at the specified position.
     * @param position Position to get item from.
     * @return Item at the specified position.
     */
    public Optional<WearableListItem> getItem(int position) {
        if (!getItems().isPresent()) {
            return new Optional<>();
        }

        ArrayList<WearableListItem> items = getItems().value;
        if (position < 0 || position > items.size() - 1) {
            return new Optional<>();
        }

        return new Optional<>(items.get(position));
    }

    /**
     * Items shown in the list view must conform to this protocol.
     */
    public interface WearableListItem {
        Optional<Integer> getIconResource();
        Optional<String> getTitle();
    }
}
