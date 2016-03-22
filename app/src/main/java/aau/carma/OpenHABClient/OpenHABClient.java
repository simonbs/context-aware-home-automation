package aau.carma.OpenHABClient;

import android.util.Log;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import aau.carma.Configuration;
import aau.carma.RESTClient.RESTClient;
import aau.carma.RESTClient.Result;
import aau.carma.Utilities.Consumer;
import aau.carma.Utilities.Optional;

/**
 * Client for communicating with the openHAB REST API.
 */
public class OpenHABClient extends RESTClient {
    /**
     * Initialize a client for communicating with openHAB REST API.
     */
    public OpenHABClient() {
        super(Configuration.openHABBaseURL);
    }

    /**
     * Loads all items from the openHAB instance.
     * @param done Called when the items are loaded or when we failed loading the items.
     */
    public void loadItems(Consumer<Result<ArrayList<Item>>, Void> done) {
        HashMap<String, String> params = new HashMap<>();
        params.put("recursive", "false");
        performJSONArrayRequest(Request.Method.GET, "items", params, new Consumer<JSONObject, Optional<Item>>() {
            @Override
            public Optional<Item> consume(JSONObject value) {
                try {
                    Item item = new Item(value);
                    return new Optional<>(item);
                } catch (JSONException e) {
                    Log.e(Configuration.Log, "Unable to map object: " + value);
                    Log.e(Configuration.Log, e.toString());
                }

                return new Optional<>();
            }
        }, done);
    }

    @Override
    public boolean isLoggingEnabled() {
        return true;
    }
}
