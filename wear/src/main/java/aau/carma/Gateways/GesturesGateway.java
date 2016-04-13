package aau.carma.Gateways;

import android.content.Context;

import org.json.JSONException;

import java.util.ArrayList;

import aau.carma.App;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDNNRTemplate;
import aau.carmakit.ThreeDOneCentGestureRecognizer.util.ThreeDTemplatesDataSource;
import aau.carmakit.Utilities.Consumer;
import aau.carmakit.Utilities.Func;
import aau.carmakit.Utilities.Funcable;
import aau.carmakit.Utilities.Optional;

/**
 * Gateway for accessing gestures stored in the database.
 */
public class GesturesGateway {
    /**
     * Retrieves all unique gesture names from the database.
     * @return All unique gesture names.
     */
    public static Optional<ArrayList<String>> allUniqueGestureNames() {
        try {
            ThreeDTemplatesDataSource dataSource = new ThreeDTemplatesDataSource(App.getContext());
            dataSource.open();
            ArrayList<ThreeDNNRTemplate> gestureTemplates = dataSource.getAllTemplates();
            dataSource.close();

            // Map gesture templates to labels.
            Funcable<String> gestureLabels = new Funcable<>(gestureTemplates).flatMap(new Consumer<ThreeDNNRTemplate, Optional<String>>() {
                @Override
                public Optional<String> consume(ThreeDNNRTemplate value) {
                    return new Optional<>(value.getLabel());
                }
            }).reduce(new Func.ReduceFunc<String, ArrayList<String>>() {
                // Show unique labels, i.e. no duplicates.
                @Override
                public ArrayList<String> reduce(String element, ArrayList<String> current) {
                    if (!current.contains(element)) {
                        current.add(element);
                    }

                    return current;
                }
            });

            return new Optional<>(gestureLabels.getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Optional<>();
    }
}
