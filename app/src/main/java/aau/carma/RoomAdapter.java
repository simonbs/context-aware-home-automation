package aau.carma;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import aau.carma.Library.Room;

/**
 * Adapter containing rooms.
 */
public class RoomAdapter extends ArrayAdapter<Room> {
    private static class ViewHolder {
        private TextView itemView;
    }

    private ViewHolder viewHolder;

    public RoomAdapter(Context context, int textViewResourceId, ArrayList<Room> rooms) {
        super(context, textViewResourceId, rooms);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.room_cell, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.room_cell_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Room room = getItem(position);
        if (room!= null) {
            viewHolder.itemView.setText(room.name);
        }

        return convertView;
    }
}
