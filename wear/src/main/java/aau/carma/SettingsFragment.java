package aau.carma;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import aau.carma.ConfigureAction.ConfigureActionActivity;
import aau.carma.Picker.PickerFragment;
import aau.carma.Picker.WearableListItemAdapter;
import aau.carma.TrainGesture.NameTrainGestureActivity;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment showing settings.
 */
public class SettingsFragment extends Fragment implements PickerFragment.OnPickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<WearableListItemAdapter.WearableListItem> settings = new ArrayList<>();
        settings.add(Setting.TRAIN_GESTURE);
        settings.add(Setting.CONFIGURE_ACTION);

        PickerFragment pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.settings_picker_fragment);
        pickerFragment.reloadItems(settings);
        pickerFragment.setOnPickListener(this);
    }

    @Override
    public void onPick(int position, WearableListItemAdapter.WearableListItem item, WearableListView.ViewHolder viewHolder) {
        Setting setting = (Setting)item;
        switch (setting) {
            case TRAIN_GESTURE:
                presentTrainGesture();
                break;
            case CONFIGURE_ACTION:
                presentConfigureAction();
                break;
        }
    }

    /**
     * Presents train gesture.
     */
    private void presentTrainGesture() {
        Intent intent = new Intent(getActivity(), NameTrainGestureActivity.class);
        startActivity(intent);
    }

    /**
     * Presents configure action.
     */
    private void presentConfigureAction() {
        Intent intent = new Intent(getActivity(), ConfigureActionActivity.class);
        startActivity(intent);
    }

    /**
     * Settings list view.
     */
    private enum Setting implements WearableListItemAdapter.WearableListItem {
        TRAIN_GESTURE,
        CONFIGURE_ACTION;

        @Override
        public Optional<Integer> getIconResource() {
            switch (this) {
                case TRAIN_GESTURE: return new Optional<>(R.drawable.gesture);
                case CONFIGURE_ACTION: return new Optional<>(R.drawable.action);
            }

            return new Optional<>();
        }

        @Override
        public Optional<String> getTitle() {
            switch (this) {
                case TRAIN_GESTURE:
                    return new Optional<>(App.getContext().getString(R.string.settings_train_gesture));
                case CONFIGURE_ACTION:
                    return new Optional<>(App.getContext().getString(R.string.settings_configure_action));
            }

            return new Optional<>();
        }
    }
}
