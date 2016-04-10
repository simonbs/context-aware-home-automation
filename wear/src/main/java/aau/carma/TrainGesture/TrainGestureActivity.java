package aau.carma;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer.ThreeDOneCentRecognizer;
import aau.carmakit.Utilities.Logger;

/**
 * Activity for training a gesture.
 */
public class TrainGestureActivity extends Activity implements SensorEventListener {
    /**
     * Intent extra key for specifying the gesture name,
     */
    public final static String IntentExtraKeyGestureName = "gesture_name";

    /**
     * Minimum amount of samples that must be taken before the gesture can be saved.
     */
    private final static int MinimumSampleCount = 1;

    /**
     * Name of the gesture to train.
     */
    private String gestureName;

    /**
     * Whether or not we are currently sampling.
     */
    private boolean isSampling;

    /**
     * Current sample count.
     */
    private int sampleCount = 0;

    /**
     * Manages the sensors.
     */
    private SensorManager sensorManager;

    /**
     * Accelerometer sensor. Samples from the sensor is
     * used in the strokes.
     */
    private Sensor accelerometerSensor;

    /**
     * Stroke currently being recognized.
     */
    private ThreeDLabeledStroke tempStroke;

    /**
     * Recognized strokes ("samples").
     */
    private ArrayList<ThreeDLabeledStroke> strokes;

    /**
     * Gesture recognizer.
     */
    private ThreeDOneCentRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_gesture);
        gestureName = getIntent().getStringExtra(IntentExtraKeyGestureName);
        showSampleCount(sampleCount);

        TextView nameTextView = (TextView)findViewById(R.id.train_gesture_name);
        nameTextView.setText(gestureName);

        final Button doneButton = (Button)findViewById(R.id.train_gesture_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        recognizer = new ThreeDOneCentRecognizer(this);
    }

    /**
     * Shows the sample count in the interface.
     * @param sampleCount Sample count.
     */
    private void showSampleCount(int sampleCount) {
        TextView sampleCountTextView = (TextView)findViewById(R.id.train_gesture_sample_count);
        sampleCountTextView.setText(String.format(getString(R.string.train_gesture_sample_count), sampleCount));
    }

    /**
     * Increments the sample count and shows the new count.
     */
    private void incrementSampleCount() {
        sampleCount += 1;
        showSampleCount(sampleCount);
        Button doneButton = (Button)findViewById(R.id.train_gesture_done);
        doneButton.setEnabled(sampleCount >= MinimumSampleCount);
    }

    /**
     * Toggles sampling a gesture.
     */
    private void toggleSampling() {
        View contentView = findViewById(android.R.id.content);
        if (isSampling) {
            // Finish this sample.
            sensorManager.unregisterListener(this);
            tempStroke = null;
            contentView.setBackgroundColor(getResources().getColor(R.color.black));
            incrementSampleCount();
            isSampling = false;
        } else {
            // Start sampling.
            isSampling = true;
            contentView.setBackgroundColor(getResources().getColor(R.color.orange));
            tempStroke = new ThreeDLabeledStroke(gestureName);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            toggleSampling();
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isSampling && tempStroke != null) {
            tempStroke.addPoint(new ThreeDPoint(event.values[0], event.values[1], event.values[2], event.timestamp));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
