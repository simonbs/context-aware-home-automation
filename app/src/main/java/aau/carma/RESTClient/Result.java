package aau.carma.RESTClient;

import com.android.volley.VolleyError;

import aau.carma.Library.Optional;

/**
 * Result returned by the openHAB client when a request has been completed.
 */
public class Result<T> {
    /**
     * Value of the result.
     */
    public final Optional<T> value;

    /**
     * Error of the result.
     */
    public final Optional<VolleyError> error;

    /**
     * Initialize a result with a value.
     * @param value Value to initialize result with.
     */
    Result(T value) {
        this.value = new Optional<>(value);
        this.error = new Optional<>();
    }

    /**
     * Initialize the result with an error.
     * @param error Error to initialize result with.
     */
    Result(VolleyError error) {
        this.value = new Optional<>();
        this.error = new Optional<>(error);
    }

    /**
     * Checks if the result indicates a success.
     */
    public boolean isSuccess() {
        return !error.isPresent();
    }

    /**
     * Checks if the result indicates an error.
     */
    public boolean isError() {
        return error.isPresent();
    }
}
