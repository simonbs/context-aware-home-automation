package aau.carma;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class SelectRoomAndActionActivity extends AppCompatActivity {

    public static String CONFIGURATION_COUNT = "configurationCount";

    private ArrayList<String[]> newConfigurations = new ArrayList<>();
    private String selectedRoom;
    private String selectedAction;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_room_and_action);
        intent = getIntent();
        if (!intent.hasExtra(SelectGestureActivity.GESTURE_LABEL_EXTRA)){
            setResult(RESULT_CANCELED);
            finish();
        }
        final ListView roomsListView = (ListView) findViewById(R.id.rooms_listView);
        final ListView actionsListView = (ListView) findViewById(R.id.actions_listView);
        ArrayAdapter<String> roomsAdapter = new ArrayAdapter<String>(this, R.layout.simple_listview_item);
        for (Room room : DummyData.getAllRooms()){
            roomsAdapter.add(room.name);
        }
        roomsListView.setAdapter(roomsAdapter);
        actionsListView.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_listview_item, DummyData.getAllActions()));
        roomsListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        actionsListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        roomsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = parent.getItemAtPosition(position).toString();
            }
        });
        actionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedAction = parent.getItemAtPosition(position).toString();
            }
        });

        ImageButton bindButton = (ImageButton) findViewById(R.id.bind_button);
        bindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If at least one selection is missing, return
                if (selectedAction == null || selectedRoom == null){
                    return;
                }
                newConfigurations.add(new String[]{selectedRoom, selectedAction, intent.getStringExtra(SelectGestureActivity.GESTURE_LABEL_EXTRA)});
            }
        });

        Button doneBindingButton = (Button) findViewById(R.id.done_binding_button);
        doneBindingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newConfigurations == null || newConfigurations.isEmpty()){
                    setResult(RESULT_OK);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt(CONFIGURATION_COUNT, newConfigurations.size());
                    for (int i = 0; i < newConfigurations.size(); i++){
                        bundle.putStringArray(Integer.toString(i), newConfigurations.get(i));
                    }
                    Intent returnIntent = new Intent();
                    returnIntent.putExtras(bundle);
                    setResult(RESULT_OK, returnIntent);
                }
                    finish();
            }
        });
    }
}
