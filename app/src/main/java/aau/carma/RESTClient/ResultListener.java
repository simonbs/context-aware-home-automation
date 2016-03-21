package aau.carma.RESTClient;

/**
 * Objects conforming to the protocol are notified when
 * the REST client has a result or an error.
 */
public interface ResultListener {
    /**
     * Called when there is a result.
     */
    <T> void onResult(Result<T> result);
}
