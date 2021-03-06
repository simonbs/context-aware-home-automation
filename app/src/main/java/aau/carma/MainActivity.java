package aau.carma;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextRecognizer;
import aau.carma.ContextEngine.ContextRecognizerListener;
import aau.carma.Database.DatabaseHelper;
import aau.carma.Library.Action;
import aau.carma.Library.ActionsManager;
import aau.carma.Library.BooleanResult;
import aau.carma.Library.Logger;
import aau.carma.Library.Result;
import aau.carma.OpenHABClient.OpenHABClient;
import aau.carma.RESTClient.BooleanResultListener;
import aau.carma.RESTClient.RESTClient;
import aau.carma.RESTClient.ResultListener;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDOneCentRecognizer;

public class MainActivity extends AppCompatActivity implements ContextRecognizerListener {
    /** Accelerometer sensor*/
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isRecording = false;
    private ThreeDLabeledStroke tempStroke;
    private ThreeDOneCentRecognizer gestureRecognizer;
    /** Default gesture label*/
    private static final String DEFAULT_LABEL = "DefaultLabel";
    static final int ADD_NEW_GESTURES_REQUEST = 1;
    static final int BIND_GESTURES_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gestureRecognizer = new ThreeDOneCentRecognizer(this);
        findViewById(R.id.recognize_gesture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizeGesture();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.add_gesture_menu_item:
                // User chose the "add gesture" item, show the add gesture UI...
                intent = new Intent(this, AddGestureActivity.class);
                startActivityForResult(intent, ADD_NEW_GESTURES_REQUEST);
                return true;
            case R.id.bind_gesture_menu_item:
                intent = new Intent(this, GestureConfigurationOptionsActivity.class);
                startActivityForResult(intent, BIND_GESTURES_REQUEST);
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ADD_NEW_GESTURES_REQUEST) {
            gestureRecognizer.loadTemplates();
        }
        if (requestCode == BIND_GESTURES_REQUEST) {
            if (resultCode == RESULT_OK) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    public void onContextReady(ArrayList<ContextOutcome> outcomes) {
        Logger.verbose("- - - - - - - - - - - - - - - - - -");
        Logger.verbose("Context is ready:");

        if (outcomes.size() > 0) {
            for (ContextOutcome outcome : outcomes) {
                Logger.verbose(outcome.id + ": " + outcome.probability);
            }
        }

        Logger.verbose("- - - - - - - - - - - - - - - - - -");
    }

    @Override
    public void onFailedRecognizingContext() {
        Logger.verbose("Recognizing the context has failed");
    }

    @Override
    public void onContextRecognitionTimeout() {
        Logger.verbose("Recognizing the context has timed out");
    }

    final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Create Point
            tempStroke.addPoint(new ThreeDPoint(event.values[0], event.values[1], event.values[2], event.timestamp));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };

    /**
     * Starts recording accelerometer data and compares it with the available training templates.
     */
    private void recognizeGesture() {
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        if (isRecording){
            Logger.verbose("End recognizing gesture");
            isRecording = false;
            sensorManager.unregisterListener(sensorEventListener);
            ArrayList<ThreeDMatch> gestureMatches = gestureRecognizer.getAllMatches(tempStroke);
            for (ThreeDMatch gestureMatch : gestureMatches) {
                Logger.verbose("Recognized training template " + gestureMatch.getLabel() + " with a score of " + gestureMatch.getScore());
            }

            CARMAContextRecognizer.getInstance().getGestureContextProvider().calculateProbabilities(gestureMatches);
            try {
                Logger.verbose("Start context engine");
                CARMAContextRecognizer.getInstance().start(new ContextRecognizerListener() {
                    @Override
                    public void onContextReady(ArrayList<ContextOutcome> outcomes) {
                        // Don't do anything if we did not get any outcomes.
                        if (outcomes.size() <= 0) {
                            Logger.verbose("Got no outcomes.");
                            return;
                        }

                        double highestProbability = Double.MIN_VALUE;
                        ContextOutcome mostProbableOutCome = outcomes.get(0);
                        for (ContextOutcome outcome : outcomes) {
                            if (outcome.probability > highestProbability) {
                                highestProbability = outcome.probability;
                                mostProbableOutCome = outcome;
                            }
                        }

                        Logger.verbose("Most probable outcome ID: " + mostProbableOutCome.id);

                        GestureConfiguration configuration = databaseHelper.getGestureConfiguration(mostProbableOutCome.id);
                        Action action = databaseHelper.getAction(configuration.actionId);

                        Logger.verbose("Action for outcome: " + action.itemName + " -> " + action.newState);

                        new OpenHABClient().updateItemState(action.itemName, action.newState, new BooleanResultListener() {
                            @Override
                            public void onResult(BooleanResult result) {
                                if (result.isSuccess()) {
                                    Logger.verbose("Success");
                                } else {
                                    result.error.value.printStackTrace();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailedRecognizingContext() {
                        Logger.verbose("Context engine failed.");
                    }

                    @Override
                    public void onContextRecognitionTimeout() {
                        Logger.verbose("Context engine did timeout.");
                    }
                });
            } catch (ContextRecognizer.IsRecognizingException e) {
                Logger.error("Cannot recognize while already recognizing.");
                e.printStackTrace();
            }
        } else {
            Logger.verbose("Start recognizing gesture");
            isRecording = true;
            tempStroke = new ThreeDLabeledStroke(DEFAULT_LABEL);
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
}
