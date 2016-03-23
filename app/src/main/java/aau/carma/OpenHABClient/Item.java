package aau.carma.OpenHABClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import aau.carma.Library.Optional;
import aau.carma.RESTClient.EntityBuilder;

/**
 * Represents an Item in openHAB.
 * https://github.com/openhab/openhab/wiki/Explanation-of-items
 */
public class Item {
    /**
     * Item types in openHAB.
     */
    public enum Type {
        /**
         * A switch with ON/OFF values.
         */
        SwitchItem,

        /**
         * A dimmer with values ON/OFF, INCREASE/DECREASE or 0 - 100.
         */
        DimmerItem;

        /**
         * Raw value for a SwitchItem.
         */
        private static String RawValueSwitchItem = "SwitchItem";

        /**
         * Raw value for a DimmerItem.
         */
        private static String RawValueDimmerItem = "DimmerItem";

        /**
         * Gets the proper type from the raw value.
         * @param rawValue Raw value to convert to a type.
         * @return Type converted from raw type, null if unable to perform conversion.
         */
        public static Optional<Type> fromRawValue(String rawValue) {
            if (rawValue.equals(RawValueSwitchItem)) {
                return new Optional<>(SwitchItem);
            } else if (rawValue.equals(RawValueDimmerItem)) {
                return new Optional<>(DimmerItem);
            }

            return new Optional<>();
        }

        /**
         * Retrieves the raw string value type, i.e.
         * the one used in openHAB.
         * @return Raw item type.
         */
        public String getRawValue() {
            switch (this) {
                case SwitchItem: return RawValueSwitchItem;
                case DimmerItem: return RawValueDimmerItem;
            }

            return null;
        }

        /**
         * Retrieve a list of possible states for the type.
         * @return List of possible states for the type.
         */
        public ArrayList<String> getStates() {
            ArrayList<String> list = new ArrayList<>();
            switch (this) {
                case SwitchItem:
                    list.add("ON");
                    list.add("OFF");
                    break;
                case DimmerItem:
                    Integer step = 10;
                    list.add("ON");
                    list.add("OFF");
                    list.add("INCREASE");
                    list.add("DECREASE");
                    for (Integer val = 10; val <= 100; val += step) {
                        list.add(val.toString());
                    }
                    break;
            }

            return list;
        }
    }

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
            Optional<Type> type = Type.fromRawValue(json.getString("type"));
            return new Item(link, name, label, state, type.value);
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
     * Type of the item.
     */
    public final Type type;

    /**
     * The items state.
     */
    public final Optional<String> state;

    private Item(String link, String name, String label, Optional<String> state, Type type) {
        this.link = link;
        this.name = name;
        this.label = label;
        this.state = state;
        this.type = type;
    }
}
