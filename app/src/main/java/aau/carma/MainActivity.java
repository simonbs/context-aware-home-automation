package aau.carma;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextRecognizerListener;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDOneCentRecognizer;
import android.support.v7.widget.Toolbar;

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
        switch (item.getItemId()) {
            case R.id.add_gesture_menu_item:
                // User chose the "add gesture" item, show the add gesture UI...
                Intent intent = new Intent(this, AddGestureActivity.class);
                startActivityForResult(intent, ADD_NEW_GESTURES_REQUEST);
                return true;

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
        Log.v(Configuration.Log, "- - - - - - - - - - - - - - - - - -");
        Log.v(Configuration.Log, "Context is ready:");

        if (outcomes.size() > 0) {
            for (ContextOutcome outcome : outcomes) {
                Log.v(Configuration.Log, outcome.id + ": " + outcome.probability);
            }
        }

        Log.v(Configuration.Log, "- - - - - - - - - - - - - - - - - -");
    }

    @Override
    public void onFailedRecognizingContext() {
        Log.v(Configuration.Log, "Recognizing the context has failed");
    }

    @Override
    public void onContextRecognitionTimeout() {
        Log.v(Configuration.Log, "Recognizing the context has timed out");
    }

    final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Create Point
            tempStroke.addPoint(new ThreeDPoint(event.values[0], event.values[1], event.values[2], event.timestamp));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * Starts recording accelerometer data and compares it with the available training templates.
     */
    private void recognizeGesture() {
        if (isRecording){
            isRecording = false;
            sensorManager.unregisterListener(sensorEventListener);
            CARMAContextRecognizer.getInstance().getGestureContextProvider().calculateProbabilities(gestureRecognizer.getAllMatches(tempStroke));
        } else {
            isRecording = true;
            tempStroke = new ThreeDLabeledStroke(DEFAULT_LABEL);
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
}
