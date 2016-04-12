package aau.carma;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.ContextEngine.ContextRecognizer;
import aau.carmakit.ContextEngine.ContextRecognizerListener;
import aau.carmakit.Database.DatabaseHelper;
import aau.carmakit.GestureConfiguration;
import aau.carmakit.OpenHABClient.OpenHABClient;
import aau.carmakit.RESTClient.BooleanResultListener;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDLabeledStroke;
import aau.carmakit.ThreeDOneCentGestureRecognizer.datatype.ThreeDPoint;
import aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer.ThreeDMatch;
import aau.carmakit.ThreeDOneCentGestureRecognizer.recognizer.ThreeDOneCentRecognizer;
import aau.carmakit.Utilities.Action;
import aau.carmakit.Utilities.BooleanResult;
import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for recognizing a gesture.
 */
public class RecognizeGestureFragment extends Fragment implements View.OnTouchListener, SensorEventListener, ContextRecognizerListener {
    /**
     * Default stroke label.
     */
    private static final String STROKE_DEFAULT_LABEL = "DefaultLabel";

    /**
     * Whether or not we are currently recognizing a gesture.
     */
    private boolean isRecognizing = false;

    /**
     * Whether or not to ignore the next touch up event.
     */
    private boolean shouldIgnoreNextTouchUpEvent = false;

    /**
     * Object to inform when recognition begins or ends.
     */
    private Optional<RecognitionListener> recognitionListener;

    /**
     * Manages sensors.
     */
    private SensorManager sensorManager;

    /**
     * Accelerometer sensor. X, Y and Z accelerations are retrieved from the sensor
     * in order to perform gesture recognition.
     */
    private Sensor accelerometerSensor;

    /**
     * Temporary stroke.
     */
    private ThreeDLabeledStroke tempStroke;

    /**
     * Gesture recognizer.
     */
    private ThreeDOneCentRecognizer gestureRecognizer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognize_gesture, container, false);
        view.setOnTouchListener(this);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gestureRecognizer = new ThreeDOneCentRecognizer(getActivity());
        return view;
    }

    /**
     * Sets object to notify when recognition begins or ends.
     * @param recognitionListener Object to notify.
     */
    public void setRecognitionListener(RecognitionListener recognitionListener) {
        this.recognitionListener = new Optional<>(recognitionListener);
    }

    /**
     * Toggles recognizing a gesture.
     */
    private void toggleRecognizing() {
        if (isRecognizing) {
            endRecognizing();
        } else {
            beginRecognizing();
        }
    }

    /**
     * Begin recognizing.
     */
    private void beginRecognizing() {
        isRecognizing = true;
        getView().setBackgroundColor(getResources().getColor(R.color.orange));
        if (recognitionListener.isPresent()) {
            recognitionListener.value.onBeginRecognizing();
        }

        tempStroke = new ThreeDLabeledStroke(STROKE_DEFAULT_LABEL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * End recognizing.
     */
    private void endRecognizing() {
        getView().setBackgroundColor(getResources().getColor(R.color.black));
        if (recognitionListener.isPresent()) {
            recognitionListener.value.onEndRecognizing();
        }

        sensorManager.unregisterListener(this);
        ArrayList<ThreeDMatch> gestureMatches = gestureRecognizer.getAllMatches(tempStroke);
        for (ThreeDMatch gestureMatch : gestureMatches) {
            Logger.verbose("Recognized training template " + gestureMatch.getLabel() + " with a score of " + gestureMatch.getScore());
        }

        CARMAContextRecognizer.getInstance().getGestureContextProvider().calculateProbabilities(gestureMatches);
        isRecognizing = false;

        recognizeContext();
    }

    /**
     * Starts context recognition.
     */
    private void recognizeContext() {
        try {
            Logger.verbose("Start context engine");
            CARMAContextRecognizer.getInstance().start(this);
        } catch (ContextRecognizer.IsRecognizingException e) {
            Logger.error("Cannot recognize while already recognizing.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Ignore next touch up event if the user is moving his finger
        // or the event was for some reason cancelled and we are
        // currently recognizing a gesture.
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (isRecognizing) {
                shouldIgnoreNextTouchUpEvent = true;
            }

            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!shouldIgnoreNextTouchUpEvent) {
                toggleRecognizing();
            }

            shouldIgnoreNextTouchUpEvent = false;
        }

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        tempStroke.addPoint(new ThreeDPoint(event.values[0], event.values[1], event.values[2], event.timestamp));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

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

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getActivity());
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

    /**
     * Objects interested in getting informed when recognition begins or ends
     * should conform to the protocol.
     */
    public interface RecognitionListener {
        /**
         * Called when recognition begins.
         */
        void onBeginRecognizing();

        /**
         * Called when recognition ends.
         */
        void onEndRecognizing();
    }
}
