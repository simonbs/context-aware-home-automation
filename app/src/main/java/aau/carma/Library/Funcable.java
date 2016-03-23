package aau.carma.Library;

import com.android.internal.util.Predicate;

import java.util.ArrayList;

import aau.carma.OpenHABClient.Thing;

/**
 * A funcable which we can perform "functional" operations on.
 * @param <T> Type of objects encapsulated by the funcable
 */
public class Funcable<T> {
    /**
     * Backing array list.
     */
    private final ArrayList<T> list;

    /**
     * Initializes a funcable with a backing array list.
     * @param list Backing array list.
     */
    Funcable(ArrayList<T> list) {
        this.list = list;
    }

    /**
     * Retrieves the value of the funcable
     * @return Value of funcable.
     */
    public ArrayList<T> getValue() {
        return list;
    }

    /**
     * Filters the backing list.
     * @param predicate Predicate to use for filtering.
     * @return Funcable with array list of filtered objects.
     */
    public Funcable<T> filter(Predicate<T> predicate) {
        return new Funcable(Func.filter(list, predicate));
    }

    /**
     * Flat maps the backing list.
     * @param <U> Type of objects to map to.
     * @param func Function to use for mapping.
     * @return Funcable with the array list of mapped objects.
     */
    public <U> Funcable<U> flatMap(Consumer<T, Optional<U>> func) {
        return new Funcable(Func.flatMap(list, func));
    }

    /**
     * Reduce the elements in the backing list.
     * @param func Function to perform the reduction.
     * @param <U> Type of new elements.
     * @return Funcable with the array list of the reduced object.
     */
    public <U> Funcable<U> reduce(Func.ReduceFunc<T, ArrayList<U>> func) {
        return new Funcable(Func.reduce(list, new ArrayList<U>(), func));
    }
}