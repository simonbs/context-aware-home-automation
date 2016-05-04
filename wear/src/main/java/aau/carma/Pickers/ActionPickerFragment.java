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
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.ActionsManager;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for picking an action.
 */
public class ActionPickerFragment extends Fragment implements PickerFragment.OnPickListener {
    /**
     * Fragment containing the picker.
     */
    private PickerFragment pickerFragment;

    /**
     * Text view informing the user that the picker is empty.
     */
    private TextView emptyTextView;

    /**
     * Listener to invoke when a action was picked.
     */
    private Optional<OnActionPickedListener> onActionPickedListener = new Optional<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action_picker, container, false);
        pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.action_picker_fragment);
        pickerFragment.setOnPickListener(this);
        emptyTextView = (TextView)view.findViewById(R.id.action_picker_empty);
        reload();
        return view;
    }

    /**
     * Sets the listener to be invoked when a action is picked.
     * @param onActionPickedListener Listener to be invoked when a action is picked.
     */
    public void setOnActionPickedListener(OnActionPickedListener onActionPickedListener) {
        this.onActionPickedListener = new Optional<>(onActionPickedListener);
    }

    /**
     * Reloads the actions and updates the picker.
     */
    private void reload() {
        Optional<ArrayList<Action>> actions = ActionsManager.getInstance().getActions();
        if (actions.isPresent() && actions.value.size() > 0) {
            Funcable<WearableListItemAdapter.WearableListItem> items = new Funcable<>(actions.value).flatMap(new Consumer<Action, Optional<WearableListItemAdapter.WearableListItem>>() {
                @Override
                public Optional<WearableListItemAdapter.WearableListItem> consume(Action value) {
                    return new Optional<>((WearableListItemAdapter.WearableListItem) new ActionPickerItem(value));
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
        if (onActionPickedListener.isPresent()) {
            onActionPickedListener.value.onPick((ActionPickerItem)item);
        }
    }

    /**
     * A action to be displayed in the picker.
     */
    public class ActionPickerItem implements WearableListItemAdapter.WearableListItem {
        /**w
         * Action encapsulated by the picker item.
         */
        public final Action action;

        /**
         * Initializes a action item for displaying in the picker.
         * @param action Encapsulated action.
         */
        ActionPickerItem(Action action) {
            this.action = action;
        }

        @Override
        public Optional<Integer> getIconResource() {
            return new Optional<>(R.drawable.action);
        }

        @Override
        public Optional<String> getTitle() {
            return new Optional<>(action.itemName);
        }

        @Override
        public Optional<String> getSubtitle() {
            return new Optional<>(action.newState);
        }
    }

    /**
     * Implemented by objects to be notified when a action is picked.
     */
    public interface OnActionPickedListener {
        /**
         * Called when a action is picked.
         * @param actionPickerItem Picked action.
         */
        void onPick(ActionPickerItem actionPickerItem);
    }
}
