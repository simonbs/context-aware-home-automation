package aau.carma.Pickers;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import aau.carma.Gateways.ActionsGateway;
import aau.carma.Gateways.GestureConfigurationsGateway;
import aau.carma.Picker.PickerFragment;
import aau.carma.Picker.WearableListItemAdapter;
import aau.carma.R;
import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for picking a gesture.
 */
public class ContextOutcomePickerFragment extends Fragment implements PickerFragment.OnPickListener {
    /**
     * Fragment containing the picker.
     */
    private PickerFragment pickerFragment;

    /**
     * Text view informing the user that the picker is empty.
     */
    private TextView emptyTextView;

    /**
     * Listener to invoke when a context outcome was picked.
     */
    private Optional<OnContextOutcomePickedListener> onContextOutcomePickedListener = new Optional<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_context_outcome_picker, container, false);
        pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.context_outcome_picker_fragment);
        pickerFragment.setOnPickListener(this);
        emptyTextView = (TextView)view.findViewById(R.id.context_outcome_picker_empty);
        reload(new ArrayList<ContextOutcome>());
        return view;
    }

    /**
     * Sets the listener to be invoked when a context outcome is picked.
     * @param onContextOutcomePickedListener Listener to be invoked when a context outcome is picked.
     */
    public void setOnContextOutcomePickedListener(OnContextOutcomePickedListener onContextOutcomePickedListener) {
        this.onContextOutcomePickedListener = new Optional<>(onContextOutcomePickedListener);
    }

    /**
     * Reloads the context outcomes and updates the picker.
     */
    public void reload(ArrayList<ContextOutcome> contextOutcomes) {
        if (contextOutcomes.size() > 0) {
            Funcable<WearableListItemAdapter.WearableListItem> items = new Funcable<>(contextOutcomes).flatMap(new Consumer<ContextOutcome, Optional<WearableListItemAdapter.WearableListItem>>() {
                @Override
                public Optional<WearableListItemAdapter.WearableListItem> consume(ContextOutcome outcome) {
                    Optional<GestureConfiguration> gestureConfiguration = GestureConfigurationsGateway.getGestureConfiguration(outcome.id);
                    if (!gestureConfiguration.isPresent()) {
                        return new Optional<>();
                    }

                    Optional<Action> action = ActionsGateway.getAction(gestureConfiguration.value.actionId);
                    if (!action.isPresent()) {
                        return new Optional<>();
                    }

                    ContextOutcomePickerItem item = new ContextOutcomePickerItem(action.value.itemName, action.value.newState, outcome);
                    return new Optional<>((WearableListItemAdapter.WearableListItem)item);
                }
            });

            pickerFragment.reloadItems(items.getValue());
            setPickerVisible(true);
        } else {
            setPickerVisible(false);
            pickerFragment.reloadItems(new ArrayList<WearableListItemAdapter.WearableListItem>());
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
        if (onContextOutcomePickedListener.isPresent()) {
            onContextOutcomePickedListener.value.onPick((ContextOutcomePickerItem)item);
        }
    }

    /**
     * Implemented by objects to be notified when a context outcome is picked.
     */
    public interface OnContextOutcomePickedListener {
        /**
         * Called when a context outcome is picked.
         * @param contextOutcomePickerItem Picked context outcome.
         */
        void onPick(ContextOutcomePickerItem contextOutcomePickerItem);
    }
}