package aau.carma;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment for recognizing a gesture.
 */
public class RecognizeGestureFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recognize_gesture_fragment, container, false);
    }
}
