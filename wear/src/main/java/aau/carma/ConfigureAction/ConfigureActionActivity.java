package aau.carma.ConfigureAction;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Activity;

import aau.carma.Pickers.ActionPickerFragment;
import aau.carma.Pickers.GesturePickerFragment;
import aau.carma.Pickers.RoomPickerFragment;
import aau.carma.R;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.Logger;

/**
 * Activity for configuring an action. Handles the configure action flow.
 */
public class ConfigureActionActivity extends Activity implements
        GesturePickerFragment.OnGesturePickedListener,
        RoomPickerFragment.OnRoomPickedListener,
        ActionPickerFragment.OnActionPickedListener  {
    /**
     * Information about the gesture, room and action the user is configuring.
     */
    private ConfigureActionInfo configureActionInfo = new ConfigureActionInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_action);
        presentGesturePicker();
    }

    /**
     * Presents the gesture picker.
     */
    private void presentGesturePicker() {
        GesturePickerFragment fragment = new GesturePickerFragment();
        fragment.setOnGesturePickedListener(this);
        replaceFragment(fragment);
    }

    /**
     * Presents the room picker.
     */
    private void presentRoomPicker() {
        RoomPickerFragment fragment = new RoomPickerFragment();
        fragment.setOnRoomPickedListener(this);
        replaceFragment(fragment);
    }

    /**
     * Presents the action picker.
     */
    private void presentActionPicker() {
        ActionPickerFragment fragment = new ActionPickerFragment();
        fragment.setOnActionPickedListener(this);
        replaceFragment(fragment);
    }

    /**
     * Saves the current gesture configuration,
     */
    private void saveGestureConfiguration() {
        if (!configureActionInfo.getGesture().isPresent()) {
            Logger.error("Tried saving a gesture configuration but no gesture has been picked.");
            return;
        }

        if (!configureActionInfo.getRoom().isPresent()) {
            Logger.error("Tried saving a gesture configuration but no room has been picked.");
            return;
        }

        if (!configureActionInfo.getAction().isPresent()) {
            Logger.error("Tried saving a gesture configuration but no action has been picked.");
            return;
        }

        GesturePickerFragment.GesturePickerItem pickedGesture = configureActionInfo.getGesture().value;
        RoomPickerFragment.RoomPickerItem pickedRoom = configureActionInfo.getRoom().value;
        ActionPickerFragment.ActionPickerItem pickedAction = configureActionInfo.getAction().value;

        Action action = DatabaseHelper.getInstance(this).saveAction(new Action(
                pickedAction.action.itemName,
                pickedAction.action.itemLabel,
                pickedAction.action.newState));
        GestureConfiguration newConfiguration = new GestureConfiguration(
                pickedRoom.room.identifier,
                action.id,
                pickedGesture.name);
        DatabaseHelper.getInstance(this).saveGestureConfiguration(newConfiguration);
        finish();
    }

    @Override
    public void onPick(GesturePickerFragment.GesturePickerItem gesturePickerItem) {
        Logger.verbose("Did pick gesture");
        configureActionInfo.setGesture(gesturePickerItem);
        presentRoomPicker();
    }

    @Override
    public void onPick(RoomPickerFragment.RoomPickerItem roomPickerItem) {
        configureActionInfo.setRoom(roomPickerItem);
        presentActionPicker();
    }

    @Override
    public void onPick(ActionPickerFragment.ActionPickerItem actionPickerItem) {
        configureActionInfo.setAction(actionPickerItem);
        saveGestureConfiguration();
    }

    /**
     * Replaces the displayed fragment.
     * @param newFragment New fragment to replace the existing fragment with.
     */
    private void replaceFragment(Fragment newFragment) {
        Logger.verbose("Replace fragment");
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.configure_action_fragment_container, newFragment, "fragment");
        transaction.commit();
    }
}
