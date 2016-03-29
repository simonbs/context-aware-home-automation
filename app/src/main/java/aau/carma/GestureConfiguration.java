package aau.carma;

/**
 * A configuration which defines which action is valid
 * in a given room when a gesture is triggered.
 */
public class GestureConfiguration {
    /**
     * Identifier for the gesture configuration
     */
    public final String id;

    /**
     * Identifier for the room in which the action is triggered.
     */
    public final String roomId;

    /**
     * Identifier for the gesture that triggers the action.
     */
    public final String gestureId;

    /**
     * Identifier for the action that should be triggered.
     */
    public final String actionId;

    public GestureConfiguration(String roomId, String actionId, String gestureId) {
        this(roomId, actionId, gestureId, "-1");
    }

    public GestureConfiguration(String roomId, String actionId, String gestureId, String id) {
        this.roomId = roomId;
        this.actionId = actionId;
        this.gestureId = gestureId;
        this.id = id;
    }
}
