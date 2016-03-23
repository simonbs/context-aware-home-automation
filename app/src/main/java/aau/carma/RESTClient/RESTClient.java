package aau.carma.RESTClient;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aau.carma.Configuration;
import aau.carma.Library.Consumer;
import aau.carma.Library.Optional;
import aau.carma.Library.Result;

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
     * @param path Path to sen the request to.
     * @param queryParams Query parameters to send with the request.
     * @param mapper Function that maps the response to objects.
     * @param done Function called when the request finishes.
     */
    public <T> void getJSONObject(String path, HashMap<String, String> queryParams, final Consumer<JSONObject, Optional<T>> mapper, final ResultListener<T> done) {
        final String url = urlWithPath(path, queryParams);
        log("[Started] GET <JSONObjectRequest>: " + url);
        Request request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                log("[Finished] GET <JSONObjectRequest>: " + url);
                Optional<T> obj = mapper.consume(response);
                if (obj.isPresent()) {
                    done.onResult(Result.Success(obj.value));
                } else {
                    VolleyError error = new VolleyError(new Throwable("Could not map object from JSON."));
                    done.onResult(Result.Failure(error));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logError("[Failed] GET <JSONObjectRequest>: " + url);
                logError(error.toString());
                done.onResult(Result.Failure(error));
            }
        });

        RequestQueue.getInstance().addToRequestQueue(request);
    }

    /**
     * Perform a JSON array request.
     * @param path Path to send the request to.
     * @param queryParams Query parameters to send with the request.
     * @param mapper Function that maps the response to objects.
     * @param done Function called when the request finishes.
     */
    public <T> void getJSONArray(String path, HashMap<String, String> queryParams, final Consumer<JSONObject, Optional<T>> mapper, final ResultListener<ArrayList<T>> done) {
        final String url = urlWithPath(path, queryParams);
        log("[Started] GET <JSONArray>: " + url);
        Request request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                log("[Finished] GET <JSONArrayRequest>: " + url);
                ArrayList<T> result = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Optional<T> obj = mapper.consume(response.getJSONObject(i));
                        if (obj.isPresent()) {
                            result.add(obj.value);
                        }
                    } catch (JSONException e) {
                        logError("Could not get JSON object at index " + i);
                        logError(e.toString());
                    }
                }

                done.onResult(Result.Success(result));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logError("[Failed] GET <JSONArrayRequest>: " + url);
                logError(error.toString());
                done.onResult(Result.Failure(error));
            }
        });

        RequestQueue.getInstance().addToRequestQueue(request);
    }

    /**
     * Performs a POST request and expects a string in return.
     * @param path Path to send the request to.
     * @param body Body to send with the request.
     * @param done Function called when the request finishes.
     */
    public void postStringRequest(String path, final String body, final ResultListener<String> done) {
        final String url = urlWithPath(path, null);
        log("[Started] POST <StringRequest>: " + url);
        final Request request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                log("[Finished] POST <StringRequest>: " + url);
                done.onResult(Result.Success(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logError("[Failed] POST <StringRequest>: " + url);
                logError(error.toString());
                done.onResult(Result.Failure(error));
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "text/plain";
            }
        };

        RequestQueue.getInstance().addToRequestQueue(request);
    }

    /**
     * Creates a URL with the specified path.
     * @param path Path to create URL for.
     * @param queryParams Set of parameters to append to the URL as query parameters.
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
     * Loads entities and maps them using the entity builder.
     * @param method HTTP method to use for the request.
     * @param path Path to load entities from.
     * @param params Query params to send with the request.
     * @param entityBuilder Builder used for mapping the entities.
     * @param done Called when the request finishes, either because of success or failure.
     * @param <T> Type of the entity to map to.
     */
    protected <T> void loadEntities(int method, String path, HashMap<String, String> params, final EntityBuilder<T> entityBuilder, ResultListener<ArrayList<T>> done) {
        getJSONArray(path, params, new Consumer<JSONObject, Optional<T>>() {
            @Override
            public Optional<T> consume(JSONObject value) {
                try {
                    T entity = entityBuilder.build(value);
                    return new Optional<>(entity);
                } catch (JSONException e) {
                    logError("Unable to map object: " + value);
                    logError(e.toString());
                }

                return new Optional<>();
            }
        }, done);
    }

    /**
     * Loads an entity and maps them using the entity builder.
     * @param method HTTP method to use for the request.
     * @param path Path to load entity from.
     * @param params Query params to send with the request.
     * @param entityBuilder Builder used for mapping the entity.
     * @param done Called when the request finishes, either because of success or failure.
     * @param <T> Type of the entity to map to.
     */
    protected <T> void loadEntity(int method, String path, HashMap<String, String> params, final EntityBuilder<T> entityBuilder,  ResultListener<T> done) {
        getJSONObject(path, params, new Consumer<JSONObject, Optional<T>>() {
            @Override
            public Optional<T> consume(JSONObject value) {
                try {
                    T entity = entityBuilder.build(value);
                    return new Optional<>(entity);
                } catch (JSONException e) {
                    logError("Unable to map object: " + value);
                    log(e.toString());
                }

                return new Optional<>();
            }
        }, done);
    }

    /**
     * Whether or not verbose logging is enabled.
     * Can be overridden by subclasses to enable logging.
     * @return Whether or not verbose logging is enabled.
     */
    public boolean isLoggingEnabled() {
        return false;
    }

    /**
     * Logs a message, if logging is enabled.
     * @param message Message to log.
     */
    private void log(String message) {
        if (isLoggingEnabled()) {
            Log.v(Configuration.Log, message);
        }
    }

    /**
     * Logs an error message, if logging is enabled.
     * @param message Error message to log.
     */
    private void logError(String message) {
        if (isLoggingEnabled()) {
            Log.e(Configuration.Log, message);
        }
    }
}
