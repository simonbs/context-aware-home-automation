package aau.carma;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Shows a picker.
 */
public class PickerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity);
    }

    /**
     * Reloads the items in the picker.
     * @param items New items to display.
     */
    public void reloadItems(ArrayList<PickerItem> items) {
        final ListView listView = (ListView)findViewById(R.id.picker_list_view);
        PickerAdapter adapter = new PickerAdapter(this, R.layout.picker_cell, items);
        listView.setAdapter(adapter);
    }
}
