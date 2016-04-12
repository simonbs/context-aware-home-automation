package aau.carma.TrainGesture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import aau.carma.R;
import aau.carma.TrainGestureActivity;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for naming a new gesture.
 */
public class NewNameTrainGestureFragment extends android.app.Fragment {
    /**
     * Speech activity request code.
     */
    private static final int SPEECH_REQUEST_CODE = 100;

    /**
     * Field containing the name of the gesture.
     */
    private Button nameButton;

    /**
     * Button for continuing.
     */
    private Button continueButton;

    /**
     * Name of the gesture to train.
     */
    private Optional<String> gestureName = new Optional<>("V");

    /**
     * Object notified when the user presses continue.
     */
    private Optional<ContinueListener> continueListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_name_train_gesture, container, false);
        nameButton = (Button)view.findViewById(R.id.new_name_train_gesture_name_button);
        continueButton = (Button)view.findViewById(R.id.new_name_train_gesture_continue_button);

        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentSpeechRecognizer();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueListener.isPresent() && gestureName.isPresent()) {
                    continueListener.value.onContinue(gestureName.value);
                }
            }
        });

        updateStateForConfiguredGestureName();

        return view;
    }

    /**
     * Set the object notified when the user presses continue.
     */
    public void setContinueListener(ContinueListener continueListener) {
        this.continueListener = new Optional<>(continueListener);
    }

    /**
     * Presents activity for speech recognition.
     */
    private void presentSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            gestureName = new Optional<>(spokenText);
            updateStateForConfiguredGestureName();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Updates the state of buttons based on the selected gesture name.
     */
    private void updateStateForConfiguredGestureName() {
        if (gestureName.isPresent()) {
            nameButton.setText(gestureName.value);
        } else {
            nameButton.setText(getString(R.string.name_train_gesture_placeholder));
        }

        continueButton.setEnabled(gestureName.isPresent() && gestureName.value.length() > 0);
    }

    /**
     * Objects should conform to the protocol in order to be notified when the user continues.
     */
    public interface ContinueListener {
        /**
         * Called when the user presses the continue button with a valid gesture name.
         * @param gestureName Name of gesture to train.
         */
        void onContinue(String gestureName);
    }
}
