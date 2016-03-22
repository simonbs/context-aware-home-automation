package aau.carma;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SelectGestureActivity extends AppCompatActivity {

    public static final int SELECT_ROOM_AND_ACTION_REQUEST = 1;
    public static final String GESTURE_LABEL_EXTRA = "gestureLabel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gesture);
        ListView listView = (ListView) findViewById(R.id.select_gesture_listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_listview_item);
        ArrayList<String> gestureLabels = DummyData.getAllGestures();
        adapter.addAll(gestureLabels);
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
