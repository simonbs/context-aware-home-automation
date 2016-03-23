package aau.carma.Library;

/**
 * Encapsulates a new state that can be set on an item.
 */
public class Action {
    /**
     * Name of the item the action can be triggered on.
     */
    public final String itemName;

    /**
     * Label of the item the action can be triggered on.
     */
    public final String itemLabel;

    /**
     * New state the item should be put in when the action is triggered.
     */
    public final String newState;

    /**
     * Initializes an action that encapsulates an item using its name
     * and a new state the item can be put in.
     * @param itemName Name of the item the action can be triggered on.
     * @param itemLabel Label of the item the action can be triggered on.
     * @param newState New state the item should be put in when the action is triggered.
     */
    public Action(String itemName, String itemLabel, String newState) {
        this.itemName = itemName;
        this.itemLabel = itemLabel;
        this.newState = newState;
    }
}
