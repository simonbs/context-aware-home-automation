package aau.carma;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import aau.carmakit.Utilities.Optional;

/**
 * Fragment showing settings.
 */
public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<WearableListItemAdapter.WearableListItem> settings = new ArrayList<>();
        settings.add(Setting.TRAIN_GESTURE);
        settings.add(Setting.CONFIGURE_ACTION);

        PickerFragment pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.settings_picker_fragment);
        pickerFragment.reloadItems(settings);
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
                case TRAIN_GESTURE: return new Optional<>(R.drawable.train_gesture);
                case CONFIGURE_ACTION: return new Optional<>(R.drawable.configure_action);
            }

            return new Optional<>();
        }

        @Override
        public Optional<Integer> getTitleResource() {
            switch (this) {
                case TRAIN_GESTURE: return new Optional<>(R.string.settings_train_gesture);
                case CONFIGURE_ACTION: return new Optional<>(R.string.settings_configure_action);
            }

            return new Optional<>();
        }
    }
}
