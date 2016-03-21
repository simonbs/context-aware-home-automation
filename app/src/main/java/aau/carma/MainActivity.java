package aau.carma;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;

import aau.carma.ContextEngine.ContextOutcome;
import aau.carma.ContextEngine.ContextRecognizer;
import aau.carma.ContextEngine.ContextRecognizerListener;
import aau.carma.ContextProviders.GestureContextProvider;
import aau.carma.ContextProviders.PositionContextProvider;
import aau.carma.OpenHABClient.Item;
import aau.carma.OpenHABClient.OpenHABClient;
import aau.carma.RESTClient.Result;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDOneCentRecognizer;
import aau.carma.Utilities.Consumer;

public class MainActivity extends AppCompatActivity implements ContextRecognizerListener {
    /** Accelerometer sensor*/
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isRecording = false;
    private ThreeDLabeledStroke tempStroke;
    private ThreeDOneCentRecognizer gestureRecognizer;
    /** Default gesture label*/
    private static final String DEFAULT_LABEL = "DefaultLabel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gestureRecognizer = new ThreeDOneCentRecognizer(this);
        findViewById(R.id.add_gesture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrainGesture(DEFAULT_LABEL);
            }
        });
        findViewById(R.id.recognize_gesture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecognizeGesture();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        // Recognize context after some seconds. For demo purposes only.
        Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                recognizeContext();
            }
        };

        Handler recognizeHandler = new Handler();
        recognizeHandler.postDelayed(timeoutRunnable, (long) (20 * 1000));

        // Load items. For demo purposes only.
        OpenHABClient client = new OpenHABClient();
        client.loadItems(new Consumer<Result<ArrayList<Item>>, Void>() {
            @Override
            public Void consume(Result<ArrayList<Item>> result) {
                if (result.isError()) {
                    Log.e(Configuration.Log, "Could not load items: " + result.error.value);
                    return null;
                }

                for (Item item : result.value.value) {
                    Log.v(Configuration.Log, item.name);
                }

                return null;
            }
        });
    }

    /**
     * Starts the context recognizer.
     */
    private void recognizeContext() {
        Log.v(Configuration.Log, "Start recognizing context");

        try {
            CARMAContextRecognizer.getInstance().start(this);
        } catch (ContextRecognizer.IsRecognizingException e) {
            Log.e(Configuration.Log, "The context recognizer could not be started because it is already started.");
        }
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
     * Starts recording accelerometer data and stores it as a training template for the gesture recognizer.
     * @param gestureLabel The name of the gesture
     */
    private void TrainGesture(String gestureLabel){
        if (isRecording){
            isRecording = false;
            sensorManager.unregisterListener(sensorEventListener);
            gestureRecognizer.AddTrainingStroke(new ThreeDLabeledStroke(tempStroke.getLabel(), tempStroke.getPoints()));
        } else {
            isRecording = true;
            tempStroke = new ThreeDLabeledStroke(gestureLabel);
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    /**
     * Starts recording accelerometer data and compares it with the available training templates.
     */
    private void RecognizeGesture(){
        if (isRecording){
            isRecording = false;
            sensorManager.unregisterListener(sensorEventListener);
            ThreeDMatch match = gestureRecognizer.recognize(tempStroke);
        } else {
            isRecording = true;
            tempStroke = new ThreeDLabeledStroke(DEFAULT_LABEL);
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }
}
