package aau.carmakit.OpenHABClient;

import com.android.volley.Request;

import java.util.ArrayList;
import java.util.HashMap;

import aau.carmakit.Configuration;
import aau.carmakit.Utilities.BooleanResult;
import aau.carmakit.Utilities.Result;
import aau.carmakit.RESTClient.BooleanResultListener;
import aau.carmakit.RESTClient.RESTClient;
import aau.carmakit.RESTClient.ResultListener;

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

    public void updateItemState(String itemName, String newState, final BooleanResultListener done)  {
        String path = "items/" + itemName;
        postStringRequest(path, newState, new ResultListener<String>() {
            @Override
            public void onResult(Result<String> result) {
                if (result.isSuccess()) {
                    done.onResult(BooleanResult.Success());
                } else {
                    done.onResult(BooleanResult.Failure(result.error.value));
                }
            }
        });
    }

    @Override
    public boolean isLoggingEnabled() {
        return true;
    }
}
