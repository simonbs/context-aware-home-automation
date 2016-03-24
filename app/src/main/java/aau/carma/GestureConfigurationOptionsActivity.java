package aau.carma;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import aau.carma.Database.DatabaseHelper;
import aau.carma.Library.Action;

public class GestureConfigurationOptionsActivity extends AppCompatActivity{

    public static final int CREATE_NEW_GESTURE_CONFIGURATIONS_REQUEST = 0;

    private GestureConfigurationListViewItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        ListView configurationsListView = (ListView) findViewById(R.id.gesture_configurations_listView);
        adapter = new GestureConfigurationListViewItemAdapter(this, DatabaseHelper.getInstance(this).getAllGestureConfiguration());
        configurationsListView.setAdapter(adapter);
        ImageButton addConfigurationButton = (ImageButton) findViewById(R.id.add_configuration_button);
        if (addConfigurationButton != null) {
            addConfigurationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewGestureConfigurations();
                }
            });
        }
    }

    private class GestureConfigurationListViewItemAdapter extends ArrayAdapter<GestureConfiguration> {

        private Context context;
        private ArrayList<GestureConfiguration> configurations;

        public GestureConfigurationListViewItemAdapter(Context context, ArrayList<GestureConfiguration> configurations) {
            super(context, R.layout.configuration_listview_item, configurations);
            this.context = context;
            this.configurations = configurations;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.configuration_listview_item, parent, false);
            }

            TextView actionTextView = (TextView) convertView.findViewById(R.id.action_textView);
            TextView roomTextView = (TextView) convertView.findViewById(R.id.room_textView);
            TextView gestureTextView = (TextView) convertView.findViewById(R.id.gesture_textView);
            final GestureConfiguration configuration = configurations.get(position);

            actionTextView.setText(DatabaseHelper.getInstance(context).getAction(Integer.parseInt(configuration.actionId)).itemLabel);
            roomTextView.setText(" at " + configuration.roomId);
            gestureTextView.setText("(" + configuration.gestureId + ")");

            ImageButton removeConfigurationButton = (ImageButton) convertView.findViewById(R.id.remove_configuration_button);

            if (removeConfigurationButton != null){
                removeConfigurationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeGestureConfiguration(configuration);
                    }
                });
            }

            return convertView;
        }

        private void removeGestureConfiguration(GestureConfiguration configuration) {
            // Remove configuration from DB and update view
            if (DatabaseHelper.getInstance(context).removeGestureConfiguration(configuration)) {
                configurations.remove(configuration);
                notifyDataSetChanged();
            }
        }
    }

    private void addNewGestureConfigurations() {
        Intent intent = new Intent(this, SelectGestureActivity.class);
        startActivityForResult(intent, CREATE_NEW_GESTURE_CONFIGURATIONS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_NEW_GESTURE_CONFIGURATIONS_REQUEST) {
            if (resultCode == RESULT_OK){
                int configurationsCount = data.getIntExtra(SelectRoomAndActionActivity.CONFIGURATION_COUNT, 0);
                for (int i = 0; i < configurationsCount; i++) {
                    String[] configurationInfo = data.getStringArrayExtra(Integer.toString(i));

                    Action action = DatabaseHelper.getInstance(this).saveAction(new Action(configurationInfo[1], configurationInfo[2], configurationInfo[3]));

                    GestureConfiguration newConfiguration = new GestureConfiguration(configurationInfo[0], action.id, configurationInfo[4]);
                    // Save newConfiguration to DB
                    adapter.add(DatabaseHelper.getInstance(this).saveGestureConfiguration(newConfiguration));
                }
                setResult(RESULT_OK);
            }
        }
    }
}
