package aau.carma.Pickers;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import aau.carma.Picker.PickerFragment;
import aau.carma.Picker.WearableListItemAdapter;
import aau.carma.R;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Optional;
import aau.carmakit.Utilities.Room;
import aau.carmakit.Utilities.RoomsManager;

/**
 * Fragment for picking a room.
 */
public class RoomPickerFragment extends Fragment implements PickerFragment.OnPickListener {
    /**
     * Fragment containing the picker.
     */
    private PickerFragment pickerFragment;

    /**
     * Text view informing the user that the picker is empty.
     */
    private TextView emptyTextView;

    /**
     * Listener to invoke when a room was picked.
     */
    private Optional<OnRoomPickedListener> onRoomPickedListener = new Optional<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_picker, container, false);
        pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.room_picker_fragment);
        pickerFragment.setOnPickListener(this);
        emptyTextView = (TextView)view.findViewById(R.id.room_picker_empty);
        reload();
        return view;
    }

    /**
     * Sets the listener to be invoked when a room is picked.
     * @param onRoomPickedListener Listener to be invoked when a room is picked.
     */
    public void setOnRoomPickedListener(OnRoomPickedListener onRoomPickedListener) {
        this.onRoomPickedListener = new Optional<>(onRoomPickedListener);
    }

    /**
     * Reloads the rooms and updates the picker.
     */
    private void reload() {
        Optional<ArrayList<Room>> rooms = RoomsManager.getInstance().getRooms();
        if (rooms.isPresent() && rooms.value.size() > 0) {
            Funcable<WearableListItemAdapter.WearableListItem> items = new Funcable<>(rooms.value).flatMap(new Consumer<Room, Optional<WearableListItemAdapter.WearableListItem>>() {
                @Override
                public Optional<WearableListItemAdapter.WearableListItem> consume(Room value) {
                    return new Optional<>((WearableListItemAdapter.WearableListItem) new RoomPickerItem(value));
                }
            });

            pickerFragment.setItems(items.getValue());
            setPickerVisible(true);
        } else {
            setPickerVisible(false);
            pickerFragment.setItems(new ArrayList<WearableListItemAdapter.WearableListItem>());
        }
    }

    /**
     * Change the visibility of the picker.
     * @param visible Whether or not the picker should be visible.
     */
    private void setPickerVisible(Boolean visible) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (visible) {
            emptyTextView.setVisibility(View.GONE);
            transaction.show(pickerFragment);
        } else {
            emptyTextView.setVisibility(View.VISIBLE);
            transaction.hide(pickerFragment);
        }
        transaction.commit();
    }

    @Override
    public void onPick(int position, WearableListItemAdapter.WearableListItem item, WearableListView.ViewHolder viewHolder) {
        if (onRoomPickedListener.isPresent()) {
            onRoomPickedListener.value.onPick((RoomPickerItem)item);
        }
    }

    /**
     * A room to be displayed in the picker.
     */
    public class RoomPickerItem implements WearableListItemAdapter.WearableListItem {
        /**w
         * Room encapsulated by the picker item.
         */
        public final Room room;

        /**
         * Initializes a room item for displaying in the picker.
         * @param room Encapsulated room.
         */
        RoomPickerItem(Room room) {
            this.room = room;
        }

        @Override
        public Optional<Integer> getIconResource() {
            return new Optional<>(R.drawable.room);
        }

        @Override
        public Optional<String> getTitle() {
            return new Optional<>(room.name);
        }

        @Override
        public Optional<String> getSubtitle() {
            return new Optional<>();
        }
    }

    /**
     * Implemented by objects to be notified when a room is picked.
     */
    public interface OnRoomPickedListener {
        /**
         * Called when a room is picked.
         * @param roomPickerItem Picked room.
         */
        void onPick(RoomPickerItem roomPickerItem);
    }
}
