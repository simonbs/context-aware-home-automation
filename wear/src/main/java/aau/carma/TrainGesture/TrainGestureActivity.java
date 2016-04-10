package aau.carma;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import aau.carmakit.Utilities.Logger;

/**
 * Activity for training a gesture.
 */
public class TrainGestureActivity extends Activity {
    /**
     * Intent extra key for specifying the gesture name,
     */
    public final static String IntentExtraKeyGestureName = "gesture_name";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_gesture);
        gestureName = getIntent().getStringExtra(IntentExtraKeyGestureName);
        showSampleCount(sampleCount);
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
    }

    /**
     * Toggles sampling a gesture.
     */
    private void toggleSampling() {
        View contentView = findViewById(android.R.id.content);
        if (isSampling) {
            // Finish this sample.
            isSampling = false;
            contentView.setBackgroundColor(getResources().getColor(R.color.black));
            incrementSampleCount();
        } else {
            // Start sampling.
            isSampling = true;
            contentView.setBackgroundColor(getResources().getColor(R.color.orange));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            toggleSampling();
        }

        return super.dispatchTouchEvent(ev);
    }
}
