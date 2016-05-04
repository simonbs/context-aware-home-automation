package aau.carma.Gateways;

import android.gesture.Gesture;

import java.util.ArrayList;

import aau.carma.App;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.Utilities.Optional;

/**
 * Gateway for accessing gesture configurations stored in the database.
 */
public class GestureConfigurationsGateway {
    /**
     * Retrieves the gesture configuration with the specified ID.
     * @param gestureConfigurationId ID of gesture configuration to retrieve.
     * @return Gesture configuration with the specified ID.
     */
    public static Optional<GestureConfiguration> getGestureConfiguration(String gestureConfigurationId) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getContext());
        return new Optional<>(databaseHelper.getGestureConfiguration(gestureConfigurationId));
    }

    /**
     * Gets all gesture configurations.
     * @return All gesture configurations.
     */
    public static Optional<ArrayList<GestureConfiguration>> getAllGestureConfigurations() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(App.getContext());
        return new Optional<>(databaseHelper.getAllGestureConfiguration());
    }
}
