package aau.carma.Library;

import com.android.internal.util.Predicate;

import java.util.ArrayList;

import aau.carma.OpenHABClient.Thing;

/**
 * Helper methods for Javas horrible imperative approach.
 */
public class Func {
    /**
     * Filters an array list.
     * @param list Array list to filter.
     * @param predicate Predicate to use for filtering.
     * @param <T> Type of items in the array list.
     * @return Filtered array list.
     */
    public static <T> ArrayList<T> filter(ArrayList<T> list, Predicate<T> predicate) {
        ArrayList<T> result = new ArrayList<>();
        for (T element : list) {
            if (predicate.apply(element)) {
                result.add(element);
            }
        }

        return result;
    }

    /**
     * Maps an array list consisting of objects of type T to an array list
     * consisting of objects of type U.
     * Objects will only be included in the final result if they are not null.
     * @param <T> Type of objects to map from.
     * @param <U> Type of objects to map to.
     * @param list Array list to map.
     * @param func Function to apply for mapping.
     * @return Array list of mapped objects.
     */
    public static <T, U> ArrayList<U> flatMap(ArrayList<T> list, Consumer<T, Optional<U>> func) {
        ArrayList<U> result = new ArrayList<>();
        for (T element : list) {
            Optional<U> mappedObj = func.consume(element);
            if (mappedObj.isPresent()) {
                result.add(mappedObj.value);
            }
        }

        return result;
    }
}
