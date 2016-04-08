package aau.carma;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter managing items in a wearable list view.
 */
class WearableListItemAdapter extends WearableListView.Adapter {
    private final Context context;
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

        TextView textView = (TextView) itemView.findViewById(R.id.wearable_list_item_title);
        textView.setText(context.getString(item.getTitleResource()));

            final ImageView imageView = (ImageView) itemView.findViewById(R.id.wearable_list_item_icon);
            imageView.setImageResource(item.getIconResource());
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
     * Items shown in the list view must conform to this protocol.
     */
    public interface WearableListItem {
        int getIconResource();
        int getTitleResource();
    }
}
