package aau.carmakit.Utilities;

/**
 * Encapsulates a result that can either be a success or failure
 * but cannot have a value.
 */
public class BooleanResult {
    /**
     * Error of the result.
     */
    public final Optional<Exception> error;

    /**
     * Creates a result that represents a success.
     * @return Success result.
     */
    public static BooleanResult Success() {
        return new BooleanResult();
    }

    /**
     * Creates a result that represents a failure.
     * @return Failure result.
     */
    public static BooleanResult Failure(Exception error) {
        return new BooleanResult(error);
    }

    /**
     * Initializes a boolean result that represents a success.
     */
    private BooleanResult() {
        this.error = new Optional<>();
    }

    /**
     * Initializes a boolean result that represents a failure.
     * @param error Encapsulated error.
     */
    private BooleanResult(Exception error) {
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