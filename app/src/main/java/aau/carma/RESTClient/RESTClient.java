package aau.carma.RESTClient;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.carma.Configuration;
import aau.carma.Utilities.Consumer;
import aau.carma.Utilities.Optional;

/**
 * Enables communication with the a REST API.
 */
public class RESTClient {
    /**
     * Base URL to send requests to.
     */
    final String baseURL;

    /**
     * Initialize a client for communicating with the REST API.
     * @param baseURL Base URL to send requests to.
     */
    protected RESTClient(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Perform a JSON object request.
     * @param method Method to use for the request.
     * @param path Path to sen the request to.
     */
    public <T> void performJSONObjectRequest(int method, String path, HashMap<String, String> queryParams, final Consumer<JSONObject, Optional<T>> mapper, final Consumer<Result<T>, Void> done) {
        String url = urlWithPath(path, queryParams);
        if (isLoggingEnabled()) {
            Log.v(Configuration.Log, url);
        }

        Request request = new JsonObjectRequest(method, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Optional<T> obj = mapper.consume(response);
                if (obj.isPresent()) {
                    done.consume(new Result<T>(obj.value));
                } else {
                    VolleyError error = new VolleyError(new Throwable("Could not map object from JSON."));
                    done.consume(new Result<T>(error));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                done.consume(new Result<T>(error));
            }
        });

        RequestQueue.getInstance().addToRequestQueue(request);
    }

    /**
     * Perform a JSON array request.
     * @param httpMethod Method to use for the request.
     * @param path Path to sen the request to.
     */
    public <T> void performJSONArrayRequest(int method, String path, HashMap<String, String> queryParams, final Consumer<JSONObject, Optional<T>> mapper, final Consumer<Result<ArrayList<T>>, Void> done) {
        String url = urlWithPath(path, queryParams);
        if (isLoggingEnabled()) {
            Log.v(Configuration.Log, url);
        }

        Request request = new JsonArrayRequest(method, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                ArrayList<T> result = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Optional<T> obj = mapper.consume(response.getJSONObject(i));
                        if (obj.isPresent()) {
                            result.add(obj.value);
                        }
                    } catch (JSONException e) {
                        Log.e(Configuration.Log, "Could not get JSON object at index " + i);
                    }
                }

                done.consume(new Result<>(result));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                done.consume(new Result<ArrayList<T>>(error));
            }
        });

        RequestQueue.getInstance().addToRequestQueue(request);
    }

    /**
     * Creates a URL with the specified path.
     * @param path Path to create URL for.
     * @return URL.
     */
    private String urlWithPath(String path, HashMap<String, String> queryParams) {
        String url;
        if (baseURL.endsWith("/") && path.startsWith("/")) {
            url = baseURL + path.substring(1);
        } else if (baseURL.endsWith("/") || path.startsWith("/")) {
            url = baseURL + path;
        } else {
            url = baseURL + "/" + path;
        }

        // Check if we should create query params.
        String params;
        if (queryParams != null) {
            // Build the params string.
            params = "?";
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                params += entry.getKey() + "=" + entry.getValue() + "&";
            }

            // Remove the last ampersand (&)
            params = params.substring(0, params.length() - 1);

            // Append the params to the URL
            url = url + params;
        }

        return url;
    }

    /**
     * Whether or not verbose logging is enabled.
     * Can be overriden by subclasses to enable logging.
     * @return Whether or not verbose logging is enabled.
     */
    public boolean isLoggingEnabled() {
        return false;
    }
}
