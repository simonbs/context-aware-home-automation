package aau.carma;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import aau.carma.Gateways.ActionsGateway;
import aau.carma.Pickers.ContextOutcomePickerActivity;
import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.ContextEngine.ContextRecognizer;
import aau.carmakit.ContextEngine.ContextRecognizerListener;
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
     * Result code for picking a context outcome.
     */
    private static final int CONTEXT_OUTCOME_PICKER_REQUEST_CODE = 1000;

    /**
     * Default stroke label.
     */
    private static final String STROKE_DEFAULT_LABEL = "DefaultLabel";

    /**
     * Outcomes are considered if their difference from the outcome with the
     * highest probability lower than or equal to this value.
     * When more than one outcome is considered, we present a picker allowing
     * the user to choose which action they want to trigger.
     */
    private static final double ContextOutcomeAcceptanceThreshold = 0.1;

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

        CARMAContextRecognizer.getInstance().getGestureContextualInformationProvider().updateProbabilities(gestureMatches);
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
            Logger.verbose("Got no outcomes");
            return;
        }

        // Sort outcomes by probability.
        Collections.sort(outcomes, new Comparator<ContextOutcome>() {
            @Override
            public int compare(ContextOutcome lhs, ContextOutcome rhs) {
                if (lhs.probability > rhs.probability) {
                    return -1;
                } else if (lhs.probability < rhs.probability) {
                    return 1;
                }

                return 0;
            }
        });

        for (ContextOutcome outcome : outcomes) {
            Logger.verbose("Context engine suggests outcome " + outcome.id + " with probability " + outcome.probability);
        }

        // Accept outcomes that have almost the same probability as the outcome
        // with the highest probability.
        ContextOutcome mostProbableOutcome = outcomes.get(0);
        ArrayList<ContextOutcome> acceptedOutcomes = new ArrayList<>();
        for (ContextOutcome outcome : outcomes) {
            if (mostProbableOutcome.probability - outcome.probability <= ContextOutcomeAcceptanceThreshold) {
                acceptedOutcomes.add(outcome);
            }
        }

        for (ContextOutcome outcome : acceptedOutcomes) {
            Logger.verbose("Accepted outcome " + outcome.id + " with probability " + outcome.probability);
        }


        if (acceptedOutcomes.size() == 1) {
            // Choose the only outcome.
            triggerActionForContextOutcome(acceptedOutcomes.get(0));
        } else {
            // Let the user pick an outcome.
            presentContextOutcomePicker(acceptedOutcomes);
            Vibrator.significantEvent();
        }
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
     * Triggers an action for a context outcome.
     * @param contextOutcome Context outcome representing the action to trigger.
     */
    private void triggerActionForContextOutcome(ContextOutcome contextOutcome) {
        Optional<Action> action = ActionsGateway.getAction(contextOutcome.id);
        if (!action.isPresent()) {
            return;
        }

        Logger.verbose("Action for outcome: " + action.value.itemName + " -> " + action.value.newState);

        new OpenHABClient().updateItemState(action.value.itemName, action.value.newState, new BooleanResultListener() {
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

    /**
     * Presents the context outcome picker with the specified context outcomes.
     * @param contextOutcomes Context outcomes to show in the picker.
     */
    private void presentContextOutcomePicker(ArrayList<ContextOutcome> contextOutcomes) {
        Intent intent = new Intent(getActivity(), ContextOutcomePickerActivity.class);
        intent.putExtra(ContextOutcomePickerActivity.EXTRA_CONTEXT_OUTCOMES, contextOutcomes);
        startActivityForResult(intent, CONTEXT_OUTCOME_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTEXT_OUTCOME_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ContextOutcome contextOutcome = data.getParcelableExtra(ContextOutcomePickerActivity.RESULT_CONTEXT_OUTCOME);
            if (contextOutcome != null) {
                triggerActionForContextOutcome(contextOutcome);
            }
        }
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
