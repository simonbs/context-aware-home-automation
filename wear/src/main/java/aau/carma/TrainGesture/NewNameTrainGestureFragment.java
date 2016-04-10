package aau.carma.TrainGesture;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import aau.carma.R;
import aau.carma.TrainGestureActivity;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for naming a new gesture.
 */
public class NewNameTrainGestureFragment extends android.app.Fragment {
    /**
     * Field containing the name of the gesture.
     */
    private EditText nameText;

    /**
     * Button for continuing.
     */
    private Button continueButton;

    /**
     * Object notified when the user presses continue.
     */
    private Optional<ContinueListener> continueListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_name_train_gesture, container, false);
        nameText = (EditText)view.findViewById(R.id.new_name_train_gesture_name_field);
        continueButton = (Button)view.findViewById(R.id.new_name_train_gesture_continue_button);

        nameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                continueButton.setEnabled(s.length() > 0);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueListener.isPresent()) {
                    String gestureName = nameText.getText().toString();
                    continueListener.value.onContinue(gestureName);
                }
            }
        });
        continueButton.setEnabled(false);

        return view;
    }

    /**
     * Set the object notified when the user presses continue.
     */
    public void setContinueListener(ContinueListener continueListener) {
        this.continueListener = new Optional<>(continueListener);
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
