package aau.carma.Gateways;

import aau.carma.App;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.Optional;

/**
 * Gateway for accessing actions stored in the database.
 */
public class ActionsGateway {
    /**
     * Retrieves the action with the specified ID.
     * @param actionId ID of action to retrieve.
     * @return Action with the specified ID.
     */
    public static Optional<Action> getAction(String actionId) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getContext());
        return new Optional<>(databaseHelper.getAction(actionId));
    }
}
