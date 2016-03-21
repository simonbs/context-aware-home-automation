package aau.carma;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carma.ThreeDOneCentGestureRecognizer.recognizer.ThreeDOneCentRecognizer;

public class AddGestureActivity extends AppCompatActivity {

    /** Accelerometer sensor*/
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isRecording = false;
    private ThreeDLabeledStroke tempStroke;
    private ArrayList<ThreeDLabeledStroke> strokes;
    private ThreeDOneCentRecognizer recognizer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gesture);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        recognizer = new ThreeDOneCentRecognizer(this);

        final EditText gestureNameInput = (EditText) findViewById(R.id.gesture_name_input);
        findViewById(R.id.train_gesture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrainGesture(gestureNameInput.getText().toString());
            }
        });
    }

    /**
     * Starts recording accelerometer data and stores it as a training template for the gesture recognizer.
     * @param gestureLabel The name of the gesture
     */
    private void TrainGesture(String gestureLabel){
        if (isRecording){
            isRecording = false;
            sensorManager.unregisterListener(sensorEventListener);
            recognizer.AddTrainingStroke(new ThreeDLabeledStroke(tempStroke.getLabel(), tempStroke.getPoints()));
        } else {
            isRecording = true;
            tempStroke = new ThreeDLabeledStroke(gestureLabel);
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
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
}
