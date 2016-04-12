package aau.carma;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import aau.carmakit.ContextEngine.ContextRecognizer;
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.ActionsManager;
import aau.carmakit.Utilities.Beacon;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Result;
import aau.carmakit.Utilities.Room;
import aau.carmakit.Utilities.RoomsManager;

/**
 * Setups the app in order to be ready to recognize gestures
 * as well as the context.
 */
public class SetupActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setup();
    }

    /**
     * Setup the application in order to be ready to
     * recognize gestures as well as recognize the context.
     */
    private void setup() {
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.setup_spinner);
        progressBar.setVisibility(View.VISIBLE);

        View errorView = findViewById(R.id.setup_error);
        errorView.setVisibility(View.GONE);

        try {
            CARMAContextRecognizer.getInstance().addGestureContextProvider(this);
        } catch (ContextRecognizer.IsRecognizingException e) {
            didFailSetup(e);
        }

//        RoomsManager.getInstance().reload(new RoomsManager.RoomsListener() {
//            @Override
//            public void onUpdate(Result<ArrayList<Room>> result) {
//                if (result.isSuccess()) {
//                    Logger.verbose("Retrieved room and beacon configuration from openHAB:");
//                    for (Room room : result.value.value) {
//                        Logger.verbose("- " + room.name);
//                        for (Beacon beacon : room.beacons) {
//                            Logger.verbose("  - " + beacon.namespace + " : " + beacon.instance);
//                        }
//                    }
//
//                    ArrayList<Room> rooms = result.value.value;
//                    try {
//                        CARMAContextRecognizer.getInstance().addPositionContextProvider(getApplicationContext(), rooms);
//                    } catch (ContextRecognizer.IsRecognizingException e) {
//                        didFailSetup(e);
//                    }
//                } else {
//                    didFailSetup(result.error.value);
//                }
//            }
//        });

        ActionsManager.getInstance().loadAllActions(new ActionsManager.ActionsListener() {
            @Override
            public void onActionsLoaded(Result<ArrayList<Action>> result) {
                if (result.isSuccess()) {
                    didSetup();
                } else {
                    didFailSetup(result.error.value);
                }
            }
        });
    }

    /**
     * Called when the setup succeeds.
     */
    private void didSetup() {
        presentMain();
    }

    /**
     * Called when the setup fails.
     * @param error Error describing why the setup failed.
     */
    private void didFailSetup(Exception error) {
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.setup_spinner);
        progressBar.setVisibility(View.GONE);

        LinearLayout errorView = (LinearLayout)findViewById(R.id.setup_error);
        errorView.setVisibility(View.VISIBLE);
    }

    /**
     * Dismisses this activity and presents the main activity.
     */
    private void presentMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

    /**
     * Retry button was clicked.
     * @param view Retry button.
     */
    public void onRetryClicked(View view) {
        setup();
    }
}
