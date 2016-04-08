package aau.carmakit.RESTClient;

import aau.carmakit.Utilities.BooleanResult;
import aau.carmakit.Utilities.Result;

/**
 * Objects conforming to the interface are informed
 * when the REST client has a result
 */
public interface BooleanResultListener {
    /**
     * Called when the REST client has result ready,
     * that being either a success or a filure.
     * @param result Boolean result from the REST client.
     */
    void onResult(BooleanResult result);
}
