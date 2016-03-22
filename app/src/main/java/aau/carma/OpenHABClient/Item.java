package aau.carma.OpenHABClient;

import org.json.JSONException;
import org.json.JSONObject;

import aau.carma.Utilities.Optional;

/**
 * Represents an Item in openHAB.
 * https://github.com/openhab/openhab/wiki/Explanation-of-items
 */
public class Item extends MappableObject {
    /**
     * The items link. Useful for interacting with the item.
     */
    public final String link;

    /**
     * The items name.
     */
    public final String name;

    /**
     * The items label.
     */
    public final String label;


    /**
     * The items state.
     */
    public final Optional<String> state;

    /**
     * Initialize an Item from a JSON representation.
     * @param json JSON representation of the object.
     * @throws JSONException Thrown when unable to map the object.
     */
    Item(JSONObject json) throws JSONException {
        super(json);
        link = json.getString("link");
        name = json.getString("name");
        label = json.getString("label");
        String state = json.getString("state");
        this.state = (state == "NULL") ? new Optional<String>() : new Optional<>(state);
    }
}
