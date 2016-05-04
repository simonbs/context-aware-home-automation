package aau.carma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Adapter for the picker activity.
 */
public class PickerAdapter extends ArrayAdapter<PickerItem> {
    /**
     * Encapsulates the subviews of the cell.
     */
    private static class ViewHolder {
        private TextView itemView;
    }

    private ViewHolder viewHolder;

    public PickerAdapter(Context context, int layoutResourceId, ArrayList<PickerItem> items) {
        super(context, layoutResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.picker_cell, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.picker_cell_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PickerItem item = getItem(position);
        if (item != null) {
            viewHolder.itemView.setText(item.getTitle());
        }

        return convertView;
    }
}
