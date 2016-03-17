package aau.carma;

import android.gesture.Gesture;

/**
 * A configuration which defines which action is valid
 * in a given room when a gesture is triggered.
 */
public class GestureConfiguration {
    /**
     * Identifier for the room in which the action is triggered.
     */
    public final String roomId;

    /**
     * Idenitifer for the gesture that triggers the action.
     */
    public final String gestureId;

    /**
     * Identifier for the action that should be triggered.
     */
    public final String actionId;

    public GestureConfiguration(String roomId, String actionId, String gestureId) {
        this.roomId = roomId;
        this.actionId = actionId;
        this.gestureId = gestureId;
    }
}
