package aau.carma;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import aau.carma.Picker.PickerFragment;
import aau.carmakit.Utilities.Optional;

/**
 * Fragment for picking a gesture.
 */
public class GesturePickerFragment extends Fragment {
    /**
     * Fragment containing the picker.
     */
    private PickerFragment pickerFragment;

    /**
     * Text view informing the user that the picker is empty.
     */
    private TextView emptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gesture_picker, container, false);
        pickerFragment = (PickerFragment)getChildFragmentManager().findFragmentById(R.id.gesture_picker_fragment);
        emptyTextView = (TextView)view.findViewById(R.id.gesture_picker_empty);
        setPickerVisible(false);
        return view;
    }

    /**
     * Change the visibility of the picker.
     * @param visible Whether or not the picker should be visible.
     */
    private void setPickerVisible(Boolean visible) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (visible) {
            emptyTextView.setVisibility(View.GONE);
            transaction.show(pickerFragment);
        } else {
            emptyTextView.setVisibility(View.VISIBLE);
            transaction.hide(pickerFragment);
        }
        transaction.commit();
    }
}
