package aau.carma;

import android.app.Fragment;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import aau.carmakit.Utilities.Logger;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for recognizing a gesture.
 */
public class RecognizeGestureFragment extends Fragment implements View.OnTouchListener {
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
    private Optional<RecognitionListener> recognitonListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recognize_gesture, container, false);
        view.setOnTouchListener(this);
        return view;
    }

    /**
     * Sets object to notify when recognition begins or ends.
     * @param recognitionListener Object to notify.
     */
    public void setRecognitionListener(RecognitionListener recognitionListener) {
        this.recognitonListener = new Optional<>(recognitionListener);
    }

    /**
     * Toggles recognizing a gesture.
     */
    private void toggleRecognizing() {
        if (isRecognizing) {
            // Finish recognizing.
            isRecognizing = false;
            getView().setBackgroundColor(getResources().getColor(R.color.black));
            if (recognitonListener.isPresent()) {
                recognitonListener.value.onEndRecognizing();
            }
        } else {
            // Start recognizing.
            isRecognizing = true;
            getView().setBackgroundColor(getResources().getColor(R.color.orange));
            if (recognitonListener.isPresent()) {
                recognitonListener.value.onBeginRecognizing();
            }
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
