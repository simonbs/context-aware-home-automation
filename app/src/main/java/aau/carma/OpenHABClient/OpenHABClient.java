package aau.carma.OpenHABClient;

import com.android.volley.Request;

import java.util.ArrayList;
import java.util.HashMap;

import aau.carma.Configuration;
import aau.carma.RESTClient.RESTClient;
import aau.carma.RESTClient.ResultListener;

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
    public void loadItems(ResultListener<ArrayList<Item>> done) {
        HashMap<String, String> params = new HashMap<>();
        params.put("recursive", "false");
        loadEntities(Request.Method.GET, "items", params, new Item.Builder(), done);
    }

    /**
     * Loads all things from the openHAB instance.
     * @param done Called when the things are loaded or when we failed loading the things.
     */
    public void loadThings(ResultListener<ArrayList<Thing>> done) {
        loadEntities(Request.Method.GET, "things", null, new Thing.Builder(), done);
    }

    @Override
    public boolean isLoggingEnabled() {
        return true;
    }
}
