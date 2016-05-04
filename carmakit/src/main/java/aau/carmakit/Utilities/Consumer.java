package aau.carmakit.Utilities;

/**
 * Consumes a value, optionally returns another.
 */
public interface Consumer<T, U> {
    U consume(T value);
}
