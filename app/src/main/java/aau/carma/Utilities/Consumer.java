package aau.carma.Utilities;

/**
 * Consumes a value, optionally returns another.
 */
public interface Consumer<T, U> {
    U consume(T value);
}
