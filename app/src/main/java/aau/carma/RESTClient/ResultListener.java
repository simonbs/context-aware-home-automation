package aau.carma.RESTClient;

import aau.carma.Library.Result;

/**
 * Objects conforming to the interface are informed
 * when the REST client has a result
 */
public interface ResultListener<T> {
    /**
     * Called when the REST client has result ready,
     * that being either a success and an entity or
     * an error.
     * @param result Result from the REST client.
     */
    void onResult(Result<T> result);
}
