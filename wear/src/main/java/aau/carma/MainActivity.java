package aau.carma;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;

import aau.carma.Gateways.ActionsGateway;
import aau.carma.Gateways.GestureConfigurationsGateway;
import aau.carma.Gateways.GesturesGateway;
import aau.carma.GridPager.GridAdapter;
import aau.carma.GridPager.GridFragmentProvider;
import aau.carma.GridPager.GridRow;
import aau.carma.GridPager.GridViewPager;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDStroke;
import aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;
import aau.carmakit.Utilities.Room;
import aau.carmakit.Utilities.RoomsManager;

public class MainActivity extends Activity implements GridFragmentProvider<MainActivity.Page>, RecognizeGestureFragment.RecognitionListener {
    /**
     * Grid pager managing the pages.
     */
    private GridViewPager gridPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridRow<Page> gridRow = new GridRow<>();
        gridRow.addPage(Page.RECOGNIZE_GESTURE);
        gridRow.addPage(Page.SETTINGS);

        ArrayList<GridRow<Page>> gridRows = new ArrayList<>();
        gridRows.add(gridRow);

        GridAdapter<Page> gridAdapter = new GridAdapter<>(getFragmentManager());
        gridAdapter.setRows(gridRows);
        gridAdapter.setFragmentProvider(this);

        gridPager = (GridViewPager) findViewById(R.id.main_pager);
        gridPager.setAdapter(gridAdapter);

        Logger.verbose("[MainActivity] Trained gestures:");
        Optional<ArrayList<String>> gestureNames = GesturesGateway.allUniqueGestureNames();
        if (gestureNames.isPresent()) {
            for (String name : gestureNames.value) {
                Logger.verbose("[MainActivity] - " + name);
            }
        } else {
            Logger.verbose("[MainActivity] - No trained gestures found.");
        }

        Logger.verbose("[MainActivity] Gesture configurations:");
        Optional<ArrayList<GestureConfiguration>> gestureConfigurations = GestureConfigurationsGateway.getAllGestureConfigurations();
        if (gestureConfigurations.isPresent()) {
            for (GestureConfiguration gestureConfiguration : gestureConfigurations.value) {
                Optional<Room> room = RoomsManager.getInstance().getRoom(gestureConfiguration.roomId);
                Optional<Action> action = ActionsGateway.getAction(gestureConfiguration.actionId);
                if (room.isPresent() && action.isPresent()) {
                    Logger.verbose(" - [MainActivity] Gesture '" + gestureConfiguration.gestureId
                            + "' in " + room.value.name + " triggers: "
                            + action.value.itemName + " -> " + action.value.newState);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    public Fragment fragmentForPage(Page page) {
        switch (page) {
            case RECOGNIZE_GESTURE: return createRecognizeGestureFragment();
            case SETTINGS: return createSettingsFragment();
        }

        return null;
    }

    @Override
    public void onBeginRecognizing() {
        gridPager.setScrollEnabled(false);
    }

    @Override
    public void onEndRecognizing() {
        gridPager.setScrollEnabled(true);
    }

    /**
     * Creates fragment for recognizing a gesture.
     * @return Created fragment.
     */
    private Fragment createRecognizeGestureFragment() {
        RecognizeGestureFragment fragment = new RecognizeGestureFragment();
        fragment.setRecognitionListener(this);
        return fragment;
    }

    /**
     * Creates fragment containing settings.
     * @return Created fragments.
     */
    private Fragment createSettingsFragment() {
        return new SettingsFragment();
    }

    /**
     * Pages in the grid pager.
     */
    public enum Page {
        RECOGNIZE_GESTURE,
        SETTINGS
    }
}
