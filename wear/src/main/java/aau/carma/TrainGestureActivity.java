package aau.carma;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity for training a new or existing gesture.
 */
public class TrainGestureActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.train_gesture_activity);
    }
}
