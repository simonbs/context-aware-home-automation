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
         * A call item.
         */
        CallItem,

        /**
         * A color item.
         */
        ColorItem,

        /**
         * A contact item.
         */
        ContactItem,

        /**
         * A datetime item.
         */
        DateTimeItem,

        /**
         * A dimmer item.
         */
        DimmerItem,

        /**
         * A group item.
         */
        GroupItem,

        /**
         * A location item.
         */
        LocationItem,

        /**
         * A number item.
         */
        NumberItem,

        /**
         * A rollershutter item.
         */
        RollershutterItem,

        /**
         * A string item.
         */
        StringItem,

        /**
         * A switch item.
         */
        SwitchItem;

        /**
         * Raw value for a CallItem.
         */
        private static String RawValueCallItem = "CallItem";

        /**
         * Raw value for a ColorItem.
         */
        private static String RawValueColorItem = "ColorItem";

        /**
         * Raw value for a ContactItem.
         */
        private static String RawValueContactItem = "ContactItem";

        /**
         * Raw value for a DateTimeItem.
         */
        private static String RawValueDateTimeItem = "DateTimeItem";

        /**
         * Raw value for a DimmerItem.
         */
        private static String RawValueDimmerItem = "DimmerItem";

        /**
         * Raw value for a GroupItem.
         */
        private static String RawValueGroupItem = "GroupItem";

        /**
         * Raw value for a LocationItem.
         */
        private static String RawValueLocationItem = "LocationItem";

        /**
         * Raw value for a LocationItem.
         */
        private static String RawValueNumberItem = "NumberItem";

        /**
         * Raw value for a RollershutterItem.
         */
        private static String RawValueRollershutterItem = "RollershutterItem";

        /**
         * Raw value for a StringItem.
         */
        private static String RawValueStringItem = "StringItem";

        /**
         * Raw value for a SwitchItem.
         */
        private static String RawValueSwitchItem = "SwitchItem";


        /**
         * Gets the proper type from the raw value.
         * @param rawValue Raw value to convert to a type.
         * @return Type converted from raw type, null if unable to perform conversion.
         */
        public static Optional<Type> fromRawValue(String rawValue) {
            if (rawValue.equals(RawValueCallItem)) {
                return new Optional<>(CallItem);
            } else if (rawValue.equals(RawValueColorItem)) {
                return new Optional<>(ColorItem);
            } else if (rawValue.equals(RawValueContactItem)) {
                return new Optional<>(ContactItem);
            } else if (rawValue.equals(RawValueDateTimeItem)) {
                return new Optional<>(DateTimeItem);
            } else if (rawValue.equals(RawValueDimmerItem)) {
                return new Optional<>(DimmerItem);
            } else if (rawValue.equals(RawValueGroupItem)) {
                return new Optional<>(GroupItem);
            } else if (rawValue.equals(RawValueNumberItem)) {
                return new Optional<>(NumberItem);
            } else if (rawValue.equals(RawValueRollershutterItem)) {
                return new Optional<>(RollershutterItem);
            } else if (rawValue.equals(RawValueStringItem)) {
                return new Optional<>(StringItem);
            } else if (rawValue.equals(RawValueSwitchItem)) {
                return new Optional<>(SwitchItem);
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
                case CallItem: return RawValueCallItem;
                case ColorItem: return RawValueColorItem;
                case ContactItem: return RawValueContactItem;
                case DateTimeItem: return RawValueDateTimeItem;
                case DimmerItem: return RawValueDimmerItem;
                case GroupItem: return RawValueGroupItem;
                case LocationItem: return RawValueLocationItem;
                case NumberItem: return RawValueNumberItem;
                case RollershutterItem: return RawValueRollershutterItem;
                case StringItem: return RawValueStringItem;
                case SwitchItem: return RawValueSwitchItem;
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
                    for (Integer val = 10; val <= 90; val += step) {
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
     * The items state.
     */
    public final Optional<String> state;

    /**
     * Type of the item.
     */
    public final Type type;

    private Item(String link, String name, String label, Optional<String> state, Type type) {
        this.link = link;
        this.name = name;
        this.label = label;
        this.state = state;
        this.type = type;
    }
}
