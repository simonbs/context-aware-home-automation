package aau.carma.Library;

/**
 * Consumes a value, optionally returns another.
 */
public interface Consumer<T, U> {
    U consume(T value);
}
