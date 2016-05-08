package aau.carma;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Set;

import aau.carma.ConfigureAction.ConfigureActionActivity;
import aau.carma.Picker.PickerFragment;
import aau.carma.Picker.WearableListItemAdapter;
import aau.carma.Pickers.RoomPickerActivity;
import aau.carma.TrainGesture.NameTrainGestureActivity;
import aau.carmakit.ContextualInformationProviders.PositionContextualInformationProvider;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDNNRTemplate;
import aau.carmakit.ThreeDOneCentGestureRecognizer.util.ThreeDTemplatesDataSource;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;
import aau.carmakit.Utilities.Room;

/**
 * Fragment showing settings.
 */
public class SettingsFragment extends Fragment implements PickerFragment.OnPickListener {
    /**
     * Activity request code for picking virtual position.
     */
    private static final int VIRTUAL_POSITION_REQUEST_CODE = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<WearableListItemAdapter.WearableListItem> settings = new ArrayList<>();
        settings.add(Setting.VIRTUAL_POSITION);
        settings.add(Setting.TRAIN_GESTURE);
        settings.add(Setting.CONFIGURE_ACTION);
        settings.add(Setting.REMOVE_TRAINED_GESTURES);

        PickerFragment pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.settings_picker_fragment);
        pickerFragment.setItems(settings);
        pickerFragment.setOnPickListener(this);
    }

    @Override
    public void onPick(int position, WearableListItemAdapter.WearableListItem item, WearableListView.ViewHolder viewHolder) {
        Setting setting = (Setting)item;
        switch (setting) {
            case VIRTUAL_POSITION:
                PositionContextualInformationProvider positionContextualInformationProvider = CARMAContextRecognizer.getInstance().getPositionContextualInformationProvider();
                if (positionContextualInformationProvider.isUsingVirtualPosition()) {
                    positionContextualInformationProvider.stopVirtualPosition();
                    reloadListView();
                } else {
                    presentVirtualPositionPicker();
                }
                break;
            case TRAIN_GESTURE:
                presentTrainGesture();
                break;
            case CONFIGURE_ACTION:
                presentConfigureAction();
            case REMOVE_TRAINED_GESTURES:
                removeTrainedGestures();
                break;
        }
    }

    /**
     * Presents the virtual position picker.
     */
    private void presentVirtualPositionPicker() {
        Intent intent = new Intent(getActivity(), RoomPickerActivity.class);
        startActivityForResult(intent, VIRTUAL_POSITION_REQUEST_CODE);
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
     * Removes all trained gestures. Useful for debugging purposes.
     */
    private void removeTrainedGestures() {
        ThreeDTemplatesDataSource dataSource = new ThreeDTemplatesDataSource(App.getContext());
        dataSource.open();
        Logger.verbose("[SettingsFragment] Will remove all trained gestured.");
        dataSource.deleteAllTemplates();
        Logger.verbose("[SettingsFragment] Did remove all trained gestures.");
        dataSource.close();
    }

    /**
     * Reloads the items in the list view.
     */
    private void reloadListView() {
        PickerFragment pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.settings_picker_fragment);
        pickerFragment.reloadItems();
    }

    /**
     * Settings list view.
     */
    private enum Setting implements WearableListItemAdapter.WearableListItem {
        VIRTUAL_POSITION,
        TRAIN_GESTURE,
        CONFIGURE_ACTION,
        REMOVE_TRAINED_GESTURES;

        @Override
        public Optional<Integer> getIconResource() {
            switch (this) {
                case VIRTUAL_POSITION: return new Optional<>(R.drawable.room);
                case TRAIN_GESTURE: return new Optional<>(R.drawable.gesture);
                case CONFIGURE_ACTION: return new Optional<>(R.drawable.action);
                case REMOVE_TRAINED_GESTURES: return new Optional<>();
            }

            return new Optional<>();
        }

        @Override
        public Optional<String> getTitle() {
            switch (this) {
                case VIRTUAL_POSITION:
                    PositionContextualInformationProvider positionContextualInformationProvider = CARMAContextRecognizer.getInstance().getPositionContextualInformationProvider();
                    if (positionContextualInformationProvider.getVirtualPosition().isPresent()) {
                        Room room = positionContextualInformationProvider.getVirtualPosition().value;
                        return new Optional<>(String.format(App.getContext().getString(R.string.settings_unset_virtual_position), room.name));
                    } else {
                        return new Optional<>(App.getContext().getString(R.string.settings_set_virtual_position));
                    }
                case TRAIN_GESTURE:
                    return new Optional<>(App.getContext().getString(R.string.settings_train_gesture));
                case CONFIGURE_ACTION:
                    return new Optional<>(App.getContext().getString(R.string.settings_configure_action));
                case REMOVE_TRAINED_GESTURES:
                    return new Optional<>(App.getContext().getString(R.string.settings_remove_trained_gestures));

            }

            return new Optional<>();
        }

        @Override
        public Optional<String> getSubtitle() {
            return new Optional<>();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIRTUAL_POSITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Room room = data.getParcelableExtra(RoomPickerActivity.RESULT_ROOM);
            if (room != null) {
                PositionContextualInformationProvider positionContextProvider = CARMAContextRecognizer.getInstance().getPositionContextualInformationProvider();
                positionContextProvider.setVirtualPosition(room);
                reloadListView();
            }
        }
    }
}
