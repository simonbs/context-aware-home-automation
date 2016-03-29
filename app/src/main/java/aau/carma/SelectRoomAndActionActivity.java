package aau.carma;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import aau.carma.Library.Action;
import aau.carma.Library.ActionsManager;
import aau.carma.Library.Logger;
import aau.carma.Library.Optional;
import aau.carma.Library.Room;
import aau.carma.Library.RoomsManager;

public class SelectRoomAndActionActivity extends AppCompatActivity {

    public static String CONFIGURATION_COUNT = "configurationCount";
    public static int RESULT_NO_ACTIONS = 2;

    private ArrayList<String[]> newConfigurations = new ArrayList<>();
    private String selectedRoom;
    private Action selectedAction;
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
            roomsAdapter.add(room.identifier);
        }
        roomsListView.setAdapter(roomsAdapter);

        Optional<ArrayList<Action>> actions = ActionsManager.getInstance().getActions();
        if (!actions.isPresent()) {
            setResult(RESULT_NO_ACTIONS);
            finish();
            return;
        }
        final ActionArrayAdapter actionArrayAdapter = new ActionArrayAdapter(this, R.layout.simple_listview_item, actions.value);
        actionsListView.setAdapter(actionArrayAdapter);

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
                selectedAction = actionArrayAdapter.getItem(position);
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
                newConfigurations.add(new String[]{selectedRoom, selectedAction.itemName, selectedAction.itemLabel, selectedAction.newState, intent.getStringExtra(SelectGestureActivity.GESTURE_LABEL_EXTRA)});
            }
        });

        Button doneBindingButton = (Button) findViewById(R.id.done_binding_button);
        doneBindingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newConfigurations == null || newConfigurations.isEmpty()){
                    setResult(RESULT_CANCELED);
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


    private class ActionArrayAdapter extends ArrayAdapter<Action> {

        private ArrayList<Action> actions = new ArrayList<>();
        private Context context;

        public ActionArrayAdapter(Context context, int resource, ArrayList<Action> actions) {
            super(context, resource, actions);
            this.actions = actions;
            this.context = context;
        }

        @Override
        public Action getItem(int position) {
            return actions.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.simple_listview_item, parent, false);
            }

            TextView actionTextView = (TextView) convertView.findViewById(R.id.gesture_label);
            Action action = actions.get(position);
            actionTextView.setText(action.itemLabel + " : " + action.newState);
            return convertView;
        }
    }
}
