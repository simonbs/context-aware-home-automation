package aau.carma.Utilities;

/**
 * Wraps an optional. Call isPresent() to check if a value is present.
 */
public class Optional<T> {
    /**
     * The wrapped value.
     */
    public final T value;

    /**
     * Initialize an optionally wrapped value.
     * @param value Value to wrap.
     */
    public Optional(T value) {
        this.value = value;
    }

    /**
     * Initializes an optional with no value.
     */
    public Optional() {
        this.value = null;
    }

    /**
     * Checks whether or not there is a value.
     * @return
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Invokes the consumer if the value is present.
     * @param consumer Consumer to invoke if value is present.
     * @param <U> Return type of consumer.
     * @return Value returned by consumer.
     */
    public <U> Optional<U> ifPresent(Consumer<T, U> consumer) {
        if (isPresent()) {
            return new Optional(consumer.consume(value));
        }

        return new Optional<>();
    }
}