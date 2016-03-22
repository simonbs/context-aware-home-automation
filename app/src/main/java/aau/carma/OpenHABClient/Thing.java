package aau.carma.OpenHABClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import aau.carma.Configuration;
import aau.carma.RESTClient.EntityBuilder;
import aau.carma.Utilities.Optional;

/**
 * Represents a Thing in openHAB.
 */
public class Thing {
    /**
     * Builds entities of type Thing.
     */
    protected static class Builder implements EntityBuilder<Thing> {
        @Override
        public Thing build(JSONObject json) throws JSONException {
            String uid = json.getString("UID");
            String thingTypeUid = json.getString("thingTypeUID");
            String link = json.getString("link");
            String label = json.getString("label");
            Thing.Configuration configuration = new Thing.Configuration.Builder().build(json.getJSONObject("configuration"));
            Thing.StatusInfo statusInfo = new Thing.StatusInfo.Builder().build(json.getJSONObject("statusInfo"));
            return new Thing(uid, thingTypeUid, link, label, configuration, statusInfo);
        }
    }

    /**
     * Contains custom configurations for a thing,
     * i.e. properties added by the developer of the
     * binding. These properties are specific to the
     * type of thing.
     */
    public static class Configuration {
        /**
         * Builds entities of type Thing.Configuration.
         */
        protected static class Builder implements EntityBuilder<Thing.Configuration> {
            @Override
            public Thing.Configuration build(JSONObject json) throws JSONException {
                HashMap<String, String> properties = new HashMap<>();
                Iterator<String> keysIterator = json.keys();
                while (keysIterator.hasNext()) {
                    String key = keysIterator.next();
                    properties.put(key, json.getString(key));
                }

                return new Thing.Configuration(properties);
            }
        }

        /**
         * Contains all the properties in the configuration.
         */
        private final HashMap<String, String> properties;

        protected Configuration(HashMap<String, String> properties) {
            this.properties = properties;
        }

        /**
         * Retrieve the value for the specified key.
         * @param key Key to retrieve value for.
         * @return Retrieved value.
         */
        public Optional<String> getValue(String key) {
            if (properties.containsKey(key)) {
                return new Optional<>(properties.get(key));
            }

            return new Optional<>();
        }
    }

    /**
     * Encapsulates the status of the thing.
     */
    public static class StatusInfo {
        /**
         * Builds entities of type Thing.StatusInfo.
         */
        protected static class Builder implements EntityBuilder<Thing.StatusInfo> {
            @Override
            public Thing.StatusInfo build(JSONObject json) throws JSONException {
                String status = json.getString("status");
                String statusDetail = json.getString("statusDetail");
                return new Thing.StatusInfo(status, statusDetail);
            }
        }

        /**
         * Status of the thing.
         */
        private final String status;

        /**
         * Detailed status.
         */
        private final String statusDetail;

        private StatusInfo(String status, String statusDetail) {
            this.status = status;
            this.statusDetail = statusDetail;
        }
    }

    /**
     * Unique identifier of the thing.
     */
    public final String uid;

    /**
     * Unique identifier for the type of thing.
     */
    public final String thingTypeUid;

    /**
     * The things link. Useful for interacting with the thing.
     */
    public final String link;

    /**
     * Label of the thing.
     */
    public final String label;

    /**
     * Configuration containing properties specific for this type of thing.
     */
    public final Thing.Configuration configuration;

    /**
     * Status of the thing.
     */
    public final Thing.StatusInfo statusInfo;

    private Thing(String uid, String thingTypeUid, String link, String label, Thing.Configuration configuration, Thing.StatusInfo statusInfo) {
        this.uid = uid;
        this.thingTypeUid = thingTypeUid;
        this.link = link;
        this.label = label;
        this.configuration = configuration;
        this.statusInfo = statusInfo;
    }
}
