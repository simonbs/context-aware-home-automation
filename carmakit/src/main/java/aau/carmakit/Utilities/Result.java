package aau.carmakit.Utilities;

/**
 * Encapsulates a result that can either be a value or an error.
 */
public class Result<T> {
    /**
     * Value of the result.
     */
    public final Optional<T> value;

    /**
     * Error of the result.
     */
    public final Optional<Exception> error;

    /**
     * Creates a result that represents a success.
     * @return Success result.
     */
    public static <T> Result Success(T value) {
        return new Result(value);
    }

    /**
     * Creates a result that represents a failure.
     * @return Failure result.
     */
    public static Result Failure(Exception error) {
        return new Result<>(error);
    }

    /**
     * Initialize a result with a value.
     * @param value Value to initialize result with.
     */
    private Result(T value) {
        this.value = new Optional<>(value);
        this.error = new Optional<>();
    }

    /**
     * Initialize the result with an error.
     * @param error Error to initialize result with.
     */
    private Result(Exception error) {
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

