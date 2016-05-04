package aau.carmakit.RESTClient;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

/**
 * Queue to which requests are added.
 */
public class RequestQueue {
    /**
     * Private instance of the request queue.
     */
    private static RequestQueue ourInstance = new RequestQueue();

    /**
     * Shared instance of the request queue.
     * @return Shared instance.
     */
    public static RequestQueue getInstance() {
        return ourInstance;
    }

    /**
     * The encapsulated request queue.
     */
    private com.android.volley.RequestQueue requestQueue;

    private RequestQueue() { }

    /**
     * Configure the request queue with the application context.
     * @param context Application context.
     */
    public void configure(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    /**
     * Add a request to the queue.
     * @param req Request to add to the queue.
     * @param <T> Type of the request.
     */
    public <T> void addToRequestQueue(Request<T> req) {
        requestQueue.add(req);
    }

    /**
     * Cancels all requests in the queue.
     */
    public void cancelAll() {
        requestQueue.cancelAll(new com.android.volley.RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}
