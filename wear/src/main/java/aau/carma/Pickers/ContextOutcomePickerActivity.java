package aau.carma.Pickers;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

import java.util.ArrayList;

import aau.carma.Picker.PickerFragment;
import aau.carma.R;
import aau.carmakit.ContextEngine.ContextOutcome;
import aau.carmakit.Utilities.Optional;

/**
 * Activity for picking a context outcome.
 */
public class ContextOutcomePickerActivity extends Activity implements ContextOutcomePickerFragment.OnContextOutcomePickedListener {
    /**
     * Key for providing context outcomes to show in the picker when starting the activity.
     */
    public static final String EXTRA_CONTEXT_OUTCOMES = "context_outcomes";

    /**
     * Key for a picked context outcome when the activity finishes with a result.
     */
    public static final String RESULT_CONTEXT_OUTCOME = "context_outcome";

    /**
     * Fragment for picking the context outcome.
     */
    private ContextOutcomePickerFragment pickerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_outcome_picker);
        pickerFragment = (ContextOutcomePickerFragment)getFragmentManager().findFragmentById(R.id.context_outcome_picker_fragment);
        pickerFragment.setOnContextOutcomePickedListener(this);

        ArrayList<ContextOutcome> contextOutcomes = getIntent().getParcelableArrayListExtra(EXTRA_CONTEXT_OUTCOMES);
        pickerFragment.reload(contextOutcomes);
    }

    @Override
    public void onPick(ContextOutcomePickerItem contextOutcomePickerItem) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RESULT_CONTEXT_OUTCOME, contextOutcomePickerItem.contextOutcome);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}
