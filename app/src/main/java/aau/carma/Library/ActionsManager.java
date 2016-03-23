package aau.carma.Library;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Arrays;

import aau.carma.OpenHABClient.Item;
import aau.carma.OpenHABClient.OpenHABClient;
import aau.carma.RESTClient.ResultListener;

/**
 * Manages the set of actions in the system.
 */
public class ActionsManager {
    /**
     * Objects conforming to the interface are notified when
     * actions has been loaded.
     */
    public interface ActionsListener {
        void onActionsLoaded(Result<ArrayList<Action>> result);
    }

    private static ActionsManager instance = new ActionsManager();

    /**
     * Shared instance of the manager.
     * @return Shared instance of ActionsManager.
     */
    public static ActionsManager getInstance(){
        return instance;
    }

    private ActionsManager(){}

    /**
     * Currently loaded actions
     */
    private Optional<ArrayList<Action>> actions = new Optional<>();

    /**
     * Gets the currently loaded actions.
     * @return Currently loaded actions.
     */
    public Optional<ArrayList<Action>> getActions() {
        return this.actions;
    }

    /**
     * Loads all supported actions in the system.
     * @param listener Listener to notify when the actions have been loaded or an error occurs.
     */
    public void loadAllActions(final ActionsListener listener) {
        OpenHABClient client = new OpenHABClient();
        client.loadItems(new ResultListener<ArrayList<Item>>() {
            @Override
            public void onResult(Result<ArrayList<Item>> result) {
                if (result.isSuccess()) {
                    didLoadItems(listener, result.value.value);
                } else {
                    didFailLoadingItems(listener, result.error.value);
                }
            }
        });
    }

    /**
     * Did load the items with success.
     * @param listener istener to inform when the actions are created.
     * @param items Items loaded.
     */
    private void didLoadItems(ActionsListener listener, ArrayList<Item> items) {
        // Create an array of arrays of actions, i.e. [[Action]].
        // [Action] is an array of actions for each items. As we have several items,
        // we also have an outer array.
        Funcable<ArrayList<Action>> actionsForItems = new Funcable(items).flatMap(new Consumer<Item, Optional<ArrayList<Action>>>() {
            @Override
            public Optional<ArrayList<Action>> consume(final Item item) {
                // Map each state to an action for the item.
                Funcable<Action> actionsForItem = new Funcable(item.type.getStates()).flatMap(new Consumer<String, Optional<Action>>() {
                    @Override
                    public Optional<Action> consume(String state) {
                        return new Optional<>(new Action(item.name, item.label, state));
                    }
                });

                return new Optional<>(actionsForItem.getValue());
            }
        });

        // Reduce the array of arrays of actions (an array for each item) to a single array of actions.
        Funcable allActions = actionsForItems.reduce(new Func.ReduceFunc<ArrayList<Action>, ArrayList<Action>>() {
            @Override
            public ArrayList<Action> reduce(ArrayList<Action> element, ArrayList<Action> current) {
                current.addAll(element);
                return current;
            }
        });

        // Store the actions for later user
        actions = new Optional<ArrayList<Action>>(allActions.getValue());

        listener.onActionsLoaded(Result.Success(allActions.getValue()));
    }

    /**
     * Loading the items failed.
     * @param listener Listener to inform that we failed loading the actions.
     * @param error Error retrieved when loading the actions failed.
     */
    private void didFailLoadingItems(ActionsListener listener, Exception error) {
        listener.onActionsLoaded(Result.Failure(error));
    }
}
