package aau.carma.ConfigureAction;

import android.gesture.Gesture;

import aau.carma.Pickers.ActionPickerFragment;
import aau.carma.Pickers.GesturePickerFragment;
import aau.carma.Pickers.RoomPickerFragment;
import aau.carmakit.Utilities.Optional;

/**
 * Holds information about an action currently being configured.
 */
public class ConfigureActionInfo {
    /**
     * The gesture the user has selected.
     */
    private Optional<GesturePickerFragment.GesturePickerItem> gesture = new Optional<>();

    /**
     * The room the user has selected.
     */
    private Optional<RoomPickerFragment.RoomPickerItem> room = new Optional<>();

    /**
     * The action the user has selected.
     */
    private Optional<ActionPickerFragment.ActionPickerItem> action = new Optional<>();

    /**
     * Sets the gesture.
     * @param gesture Gesture to set.
     */
    public void setGesture(GesturePickerFragment.GesturePickerItem gesture) {
        this.gesture = new Optional<>(gesture);
    }

    /**
     * Sets the room.
     * @param room room to set.
     */
    public void setRoom(RoomPickerFragment.RoomPickerItem room) {
        this.room = new Optional<>(room);
    }

    /**
     * Sets the action.
     * @param action Action to set.
     */
    public void setAction(ActionPickerFragment.ActionPickerItem action) {
        this.action = new Optional<>(action);
    }

    /**
     * Gets the gesture.
     * @return Gesture.
     */
    public Optional<GesturePickerFragment.GesturePickerItem> getGesture() {
        return gesture;
    }

    /**
     * Gets the room.
     * @return Room.
     */
    public Optional<RoomPickerFragment.RoomPickerItem> getRoom() {
        return room;
    }

    /**
     * Gets the action.
     * @return Action.
     */
    public Optional<ActionPickerFragment.ActionPickerItem> getAction() {
        return action;
    }
}
