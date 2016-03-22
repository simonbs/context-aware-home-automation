package aau.carma.OpenHABClient;

import org.json.JSONException;
import org.json.JSONObject;

import aau.carma.RESTClient.EntityBuilder;
import aau.carma.Utilities.Optional;

/**
 * Represents an Item in openHAB.
 * https://github.com/openhab/openhab/wiki/Explanation-of-items
 */
public class Item {
    /**
     * Builds entities of type Item.
     */
    protected static class Builder implements EntityBuilder<Item> {
        @Override
        public Item build(JSONObject json) throws JSONException {
            String link = json.getString("link");
            String name = json.getString("name");
            String label = json.getString("label");
            String rawState = json.getString("state");
            Optional<String> state = (rawState == "NULL") ? new Optional<String>() : new Optional<>(rawState);
            return new Item(link, name, label, state);
        }
    }

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

    private Item(String link, String name, String label, Optional<String> state) {
        this.link = link;
        this.name = name;
        this.label = label;
        this.state = state;
    }
}
