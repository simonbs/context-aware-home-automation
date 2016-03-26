package aau.carma;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.util.ArrayList;

import aau.carma.Library.Consumer;
import aau.carma.Library.Func;
import aau.carma.Library.Funcable;
import aau.carma.Library.Optional;
import aau.carma.ThreeDOneCentGestureRecognizer.datatype.ThreeDNNRTemplate;
import aau.carma.ThreeDOneCentGestureRecognizer.util.ThreeDTemplatesDataSource;

public class SelectGestureActivity extends AppCompatActivity {

    public static final int SELECT_ROOM_AND_ACTION_REQUEST = 1;
    public static final String GESTURE_LABEL_EXTRA = "gestureLabel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gesture);
        ListView listView = (ListView) findViewById(R.id.select_gesture_listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_listview_item);
        adapter.addAll(loadGestureLabels());
        listView.setAdapter(adapter);
        final Intent intent = new Intent(this, SelectRoomAndActionActivity.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent.putExtra(GESTURE_LABEL_EXTRA, parent.getItemAtPosition(position).toString());
                startActivityForResult(intent, SELECT_ROOM_AND_ACTION_REQUEST);
            }
        });
    }

    /**
     * Loads gesture from the database and returns them ready
     * for displaying in the picker.
     * @return Array list of gesture labels.
     */
    private ArrayList<String> loadGestureLabels() {
        try {
            ThreeDTemplatesDataSource dataSource = new ThreeDTemplatesDataSource(getApplicationContext());
            dataSource.open();
            ArrayList<ThreeDNNRTemplate> gestureTemplates = dataSource.getAllTemplates();
            dataSource.close();

            // Map gesture templates to labels.
            Funcable<String> gestureLabels = new Funcable<>(gestureTemplates).flatMap(new Consumer<ThreeDNNRTemplate, Optional<String>>() {
                @Override
                public Optional<String> consume(ThreeDNNRTemplate value) {
                    return new Optional<>(value.getLabel());
                }
            }).reduce(new Func.ReduceFunc<String, ArrayList<String>>() {
                // Show unique labels, i.e. no duplicates.
                @Override
                public ArrayList<String> reduce(String element, ArrayList<String> current) {
                    if (!current.contains(element)) {
                        current.add(element);
                    }

                    return current;
                }
            });

            return gestureLabels.getValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SELECT_ROOM_AND_ACTION_REQUEST) {
            setResult(resultCode);
            if (resultCode == RESULT_OK){
                setResult(RESULT_OK, data);
            }
            finish();
        }
    }
}
