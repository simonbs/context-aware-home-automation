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

import aau.carma.Gateways.GesturesGateway;
import aau.carma.Picker.PickerFragment;
import aau.carma.Picker.WearableListItemAdapter.WearableListItem;
import aau.carma.R;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for picking a gesture.
 */
public class GesturePickerFragment extends Fragment implements PickerFragment.OnPickListener {
    /**
     * Fragment containing the picker.
     */
    private PickerFragment pickerFragment;

    /**
     * Text view informing the user that the picker is empty.
     */
    private TextView emptyTextView;

    /**
     * Listener to invoke when a gesture was picked.
     */
    private Optional<OnGesturePickedListener> onGesturePickedListener = new Optional<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gesture_picker, container, false);
        pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.gesture_picker_fragment);
        pickerFragment.setOnPickListener(this);
        emptyTextView = (TextView)view.findViewById(R.id.gesture_picker_empty);
        reload();
        return view;
    }

    /**
     * Sets the listener to be invoked when a gesture is picked.
     * @param onGesturePickedListener Listener to be invoked when a gesture is picked.
     */
    public void setOnGesturePickedListener(OnGesturePickedListener onGesturePickedListener) {
        this.onGesturePickedListener = new Optional<>(onGesturePickedListener);
    }

    /**
     * Reloads the gestures and updates the picker.
     */
    private void reload() {
        /**
         * Enable to show a single dummy gesture.
         */
//        ArrayList<String> gestureNames = new ArrayList<>();
//        gestureNames.add("Circle");
//
//        Funcable<WearableListItem> items = new Funcable<>(gestureNames).flatMap(new Consumer<String, Optional<WearableListItem>>() {
//            @Override
//            public Optional<WearableListItem> consume(String value) {
//                return new Optional<>((WearableListItem)new GesturePickerItem(value));
//            }
//        });
//
//        pickerFragment.reloadItems(items.getValue());
//        setPickerVisible(true);

        Optional<ArrayList<String>> gestureNames = GesturesGateway.allUniqueGestureNames();
        if (gestureNames.isPresent() && gestureNames.value.size() > 0) {
            Funcable<WearableListItem> items = new Funcable<>(gestureNames.value).flatMap(new Consumer<String, Optional<WearableListItem>>() {
                @Override
                public Optional<WearableListItem> consume(String value) {
                    return new Optional<>((WearableListItem)new GesturePickerItem(value));
                }
            });

            pickerFragment.reloadItems(items.getValue());
            setPickerVisible(true);
        } else {
            setPickerVisible(false);
            pickerFragment.reloadItems(new ArrayList<WearableListItem>());
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
    public void onPick(int position, WearableListItem item, WearableListView.ViewHolder viewHolder) {
        if (onGesturePickedListener.isPresent()) {
            onGesturePickedListener.value.onPick((GesturePickerItem)item);
        }
    }

    /**
     * A gesture to be displayed in the picker.
     */
    public class GesturePickerItem implements WearableListItem {
        /**w
         * Name of the gesture.
         */
        public final String name;

        /**
         * Initializes a gesture item for displaying in the picker.
         * @param name Name of the gesture.
         */
        GesturePickerItem(String name) {
            this.name = name;
        }

        @Override
        public Optional<Integer> getIconResource() {
            return new Optional<>(R.drawable.gesture);
        }

        @Override
        public Optional<String> getTitle() {
            return new Optional<>(name);
        }

        @Override
        public Optional<String> getSubtitle() {
            return new Optional<>();
        }
    }

    /**
     * Implemented by objects to be notified when a gesture is picked.
     */
    public interface OnGesturePickedListener {
        /**
         * Called when a gesture is picked.
         * @param gesturePickerItem Picked gesture.
         */
        void onPick(GesturePickerItem gesturePickerItem);
    }
}
