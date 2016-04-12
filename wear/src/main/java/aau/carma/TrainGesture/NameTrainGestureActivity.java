package aau.carma.TrainGesture;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.wearable.view.GridViewPager;

import java.util.ArrayList;

import aau.carma.GridPager.GridAdapter;
import aau.carma.GridPager.GridFragmentProvider;
import aau.carma.GridPager.GridRow;
import aau.carma.Pickers.GesturePickerFragment;
import aau.carma.R;
import aau.carma.TrainGestureActivity;

/**
 * Activity for choosing the name of a gesture.
 */
public class NameTrainGestureActivity extends Activity implements GridFragmentProvider<NameTrainGestureActivity.Page> {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_train_gesture);

        GridRow<Page> gridRow = new GridRow<>();
        gridRow.addPage(Page.NEW_NAME);
        gridRow.addPage(Page.EXISTING_GESTURE);

        ArrayList<GridRow<Page>> gridRows = new ArrayList<>();
        gridRows.add(gridRow);

        GridAdapter<Page> gridAdapter = new GridAdapter<>(getFragmentManager());
        gridAdapter.setRows(gridRows);
        gridAdapter.setFragmentProvider(this);

        final GridViewPager gridPager = (GridViewPager) findViewById(R.id.name_train_gesture_pager);
        gridPager.setAdapter(gridAdapter);
    }

    /**
     * Starts training a gesture.
     * @param gestureName Name of gesture to train.
     */
    private void startTraining(String gestureName) {
        Intent intent = new Intent(this, TrainGestureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        intent.putExtra(TrainGestureActivity.IntentExtraKeyGestureName, gestureName);
        startActivity(intent);
        // Disable animation on transition.
        overridePendingTransition(0, 0);
        // Finish this activity as we are replacing it with the activity for training.
        finish();
    }

    @Override
    public Fragment fragmentForPage(Page page) {
        switch (page) {
            case NEW_NAME: return createNewNameFragment();
            case EXISTING_GESTURE: return createExistingGestureFragment();
        }

        return null;
    }

    /**
     * Creates fragment for training a gesture with a specified name.
     * @return Created fragment.
     */
    private Fragment createNewNameFragment() {
        NewNameTrainGestureFragment fragment = new NewNameTrainGestureFragment();
        fragment.setContinueListener(new NewNameTrainGestureFragment.ContinueListener() {
            @Override
            public void onContinue(String gestureName) {
                startTraining(gestureName);
            }
        });
        return fragment;
    }

    /**
     * Creates a fragment for training an existing gesture.
     * @return Created fragment.
     */
    private Fragment createExistingGestureFragment() {
        GesturePickerFragment fragment = new GesturePickerFragment();
        fragment.setOnGesturePickedListener(new GesturePickerFragment.OnGesturePickedListener() {
            @Override
            public void onPick(GesturePickerFragment.GesturePickerItem gesturePickerItem) {
                startTraining(gesturePickerItem.name);
            }
        });
        return fragment;
    }

    /**
     * Pages in the grid pager.
     */
    public enum Page {
        NEW_NAME,
        EXISTING_GESTURE
    }
}