package aau.carma.RESTClient;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Object that maps from a JSON object,
 */
public interface EntityBuilder<T> {
    /**
     * Builds an entity from JSON
     * @param json JSON to build entity from.
     * @return Built entity.
     * @throws JSONException Thrown if the entity cannot be built from the supplied JSON.
     */
    T build(JSONObject json) throws JSONException;
}
