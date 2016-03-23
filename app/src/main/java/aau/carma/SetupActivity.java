package aau.carma;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.lang.reflect.Array;
import java.util.ArrayList;

import aau.carma.Library.Beacon;
import aau.carma.Library.Logger;
import aau.carma.Library.Result;
import aau.carma.Library.Room;
import aau.carma.Library.RoomsManager;
import aau.carma.RESTClient.ResultListener;

/**
 * Setups the app in order to be ready to recognize gestures
 * as well as the context.
 */
public class SetupActivity extends AppCompatActivity {
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

        RoomsManager.getInstance().reload(new RoomsManager.RoomsListener() {
            @Override
            public void onUpdate(Result<ArrayList<Room>> result) {
                if (result.isSuccess()) {
                    Logger.verbose("Retrieved room and beacon configuration from openHAB:");
                    for (Room room : result.value.value) {
                        Logger.verbose("- " + room.name);
                        for (Beacon beacon : room.beacons) {
                            Logger.verbose("  - " + beacon.namespace + " : " + beacon.instance);
                        }
                    }

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
